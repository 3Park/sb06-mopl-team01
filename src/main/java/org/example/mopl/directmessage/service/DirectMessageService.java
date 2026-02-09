package org.example.mopl.directmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.directmessage.dto.condition.ConversationSearchCondition;
import org.example.mopl.directmessage.dto.condition.DirectMessageSearchCondition;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.dto.request.ConversationListRequest;
import org.example.mopl.directmessage.dto.request.DirectMessageListRequest;
import org.example.mopl.directmessage.dto.response.CursorResponseConversationDto;
import org.example.mopl.directmessage.dto.data.DirectMessageDto;
import org.example.mopl.directmessage.dto.response.CursorResponseDirectMessageDto;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.exception.ConversationForbiddenException;
import org.example.mopl.directmessage.exception.ConversationNotFoundException;
import org.example.mopl.directmessage.exception.ParticipantNotFoundException;
import org.example.mopl.directmessage.repository.ConversationRepository;
import org.example.mopl.directmessage.repository.DirectMessageRepository;
import org.example.mopl.event.message.DmMessageReceivedKafkaEvent;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    // DM 생성 및 전송
    @Transactional
    public void saveAndSendMessage(UUID conversationUuid, UUID senderUuid, String content) {

        Conversation conversation = getConversationOrThrow(conversationUuid);
        User sender = getUserOrThrow(senderUuid);

        User receiver = getCounterpartOrThrow(conversation, sender.getId());

        DirectMessage directMessage = saveMessage(conversation, sender, receiver, content);
        log.debug("메시지 저장 완료, messageId={}", directMessage.getUuid());

        sendToSocket(conversationUuid, directMessage, sender, receiver);

        eventPublisher.publishEvent(DmMessageReceivedKafkaEvent.of(
                receiver.getUuid(), sender.getProfile().getName(), content));

        log.info("메시지 전송 완료, messageId={}", directMessage.getUuid());

    }

    // 대화 생성
    @Transactional
    public ConversationDto create(UUID creatorId, UUID joinId) {

        User creator = getUserOrThrow(creatorId);
        User joiner = getUserOrThrow(joinId);

        Conversation conversation = getExistingConversation(creator, joiner)
                .orElseGet(() -> saveConversation(creator, joiner));

        log.info("conversation 생성 완료, conversationId={}", conversation.getUuid());
        return ConversationDto.from(
                conversation, creator, joiner, getLastMessageOrNull(conversation)
        );
    }

    // DM 읽음 처리
    @Transactional
    public void read(UUID conversationId, UUID lastDirectMessageId, UUID requesterId) {

        Conversation conversation = getConversationOrThrow(conversationId);
        User requester = getUserOrThrow(requesterId);

        readUnreadMessagesInAndSave(conversation, requester);

        log.info("DM 읽음 처리 완료, directMessageId={}", lastDirectMessageId);
    }

    // 대화 조회
    @Transactional(readOnly = true)
    public ConversationDto get(UUID requesterUuid, UUID conversationUuid) {

        Conversation conversation = getConversationOrThrow(conversationUuid);
        User requester = getUserOrThrow(requesterUuid);
        User other = getCounterpartOrThrow(conversation, requester.getId());

        log.info("대화 조회 완료, conversationId={}", conversationUuid);
        return ConversationDto.from(
                conversation, requester, other, getLastMessageOrNull(conversation)
        );
    }

    // 특정 사용자와의 대화 조회
    @Transactional(readOnly = true)
    public ConversationDto getWith(UUID requesterUuid, UUID withUserUuid) {

        User requester = getUserOrThrow(requesterUuid);
        User withUser = getUserOrThrow(withUserUuid);

        Conversation conversation = getExistingConversation(requester, withUser)
                .orElseThrow(ConversationNotFoundException::new);


        log.info("with={} 사용자와의 대화 조회 완료, conversationId={}", withUserUuid, conversation.getUuid());
        return ConversationDto.from(
                conversation, requester, withUser, getLastMessageOrNull(conversation)
        );
    }

    // 대화 목록 조회
    @Transactional(readOnly = true)
    public CursorResponseConversationDto getConversations(UUID requesterUuid, ConversationListRequest request) {

        User requester = getUserOrThrow(requesterUuid);

        ConversationSearchCondition condition = ConversationSearchCondition.of(
                request.keywordLike(), request.idAfter(), request.limit() + 1,
                request.sortDirection(), request.sortBy(), requester.getId()
        );

        List<Conversation> conversations = conversationRepository.searchByCursor(condition);
        Long totalCount = conversationRepository.countByUserId(requester.getId());

        // TODO: 추후 conversation 테이블에 last_message_id 컬럼 추가? 후 관련 로직 수정?
        // TODO: findAllWithProfileByIdIn(List<Long> ids) 메소드 임의로 만들어 임시 사용
        // conversation 상대방 찾기 ( key = conversationId )
        Map<Long, User> counterpartMap = findCounterpartUserByConversations(requester, conversations);
        // conversation lastMessage 찾기 ( key = conversationId )
        Map<Long, DirectMessage> lastMessageMap = findLastMessagesByConversations(conversations);

        return CursorResponseConversationDto.of(
                conversations, totalCount, condition, counterpartMap, lastMessageMap, requester
        );
    }

    // DM 목록 조회
    @Transactional(readOnly = true)
    public CursorResponseDirectMessageDto getDirectMessages(
            UUID requesterUuid, UUID conversationUuid, DirectMessageListRequest request) {

        User requester = getUserOrThrow(requesterUuid);
        Conversation conversation = getConversationOrThrow(conversationUuid);
        User other =  getCounterpartOrThrow(conversation, requester.getId());

        DirectMessageSearchCondition condition = DirectMessageSearchCondition.of(
                request.idAfter(), request.limit() + 1,
                request.sortDirection(), request.sortBy(),
                requester.getId(), conversation.getId()
        );

        List<DirectMessage> directMessages = directMessageRepository.searchByCursor(condition);
        Long totalCont = directMessageRepository.countByConversationId(conversation.getId());

        log.info("대화 목록 조회 완료, requesterId={}, conversationId={}", requesterUuid, conversationUuid);
        return CursorResponseDirectMessageDto.of(
                directMessages, totalCont, condition, requester, other
        );
    }

    private Map<Long, User> findCounterpartUserByConversations(User requester, List<Conversation> conversations) {
        List<Long> counterpartIds = conversations.stream()
                .map(c -> c.getCounterpartId(requester.getId())).toList();
        return userRepository.findAllWithProfileByIdIn(counterpartIds).stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user));
    }
    private Map<Long, DirectMessage> findLastMessagesByConversations(List<Conversation> conversations) {
        List<Long> conversationIds = conversations.stream().map(Conversation::getId).toList();
        return directMessageRepository.findAllLastMessagesByConversationIdIn(conversationIds);
    }

    private Conversation saveConversation(User creator, User joiner) {
        Conversation conversation = Conversation.of(creator.getId(), joiner.getId());
        return conversationRepository.save(conversation);
    }

    private Optional<Conversation> getExistingConversation(User user1, User user2) {
        return conversationRepository.findExisting(user1.getId(), user2.getId());
    }

    private DirectMessage getLastMessageOrNull(Conversation conversation) {
        return directMessageRepository
                .findFirstByConversationIdOrderByIdDesc(conversation.getId())
                .orElse(null);
    }

    private Conversation getConversationOrThrow(UUID conversationUuid) {
        return conversationRepository.findByUuid(conversationUuid)
                .orElseThrow(() -> new ConversationNotFoundException(conversationUuid));
    }

    private void readUnreadMessagesInAndSave(Conversation conversation, User requester) {
        directMessageRepository.readUnreadMessages(conversation.getId(), requester.getId());
    }

    private User getUserOrThrow(UUID senderUuid) {
        return userRepository.findUserAndProfileOnlyByUuid(senderUuid)
                .orElseThrow(() -> new ParticipantNotFoundException(senderUuid));
    }

    private User getCounterpartOrThrow(Conversation conversation, Long requesterId) {
        if(!conversation.isValidParticipant(requesterId)) throw new ConversationForbiddenException(conversation.getUuid());
        Long counterpartId = conversation.getCounterpartId(requesterId);
        return userRepository.findUserAndProfileOnlyById(counterpartId)
                .orElseThrow(ParticipantNotFoundException::new);
    }

    private DirectMessage saveMessage(Conversation conversation, User sender, User receiver, String content) {
        DirectMessage directMessage = DirectMessage.of(conversation, sender.getId(), receiver.getId(), content);
        return directMessageRepository.save(directMessage);
    }

    private void sendToSocket(UUID conversationUuid, DirectMessage directMessage, User sender, User receiver) {
        DirectMessageDto dto = DirectMessageDto.from(directMessage, sender, receiver);
        messagingTemplate.convertAndSend(resolveDestination(conversationUuid), dto);
    }

    private String resolveDestination(UUID conversationUuid) {
        return "/sub/conversations/" + conversationUuid + "/direct-messages";
    }
}
