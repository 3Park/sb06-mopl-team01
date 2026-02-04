package org.example.mopl.directmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.directmessage.dto.ConversationDto;
import org.example.mopl.directmessage.dto.DirectMessageDto;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.exception.ConversationNotFoundException;
import org.example.mopl.directmessage.exception.DirectMessageNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public ConversationDto create(UUID creatorId, UUID joinId) {

        User creator = getUserOrThrow(creatorId);
        User joiner = getUserOrThrow(joinId);

        Conversation conversation = getExistingConversation(creator, joiner)
                .orElseGet(() -> saveConversation(creator, joiner));

        DirectMessage lastMessage = getLastMessageOrNull(conversation);

        ConversationDto conversationDto = ConversationDto.from(conversation, creator, joiner, lastMessage);

        log.info("conversation 생성 완료, conversationId={}", conversation.getUuid());
        return conversationDto;
    }

    @Transactional
    public void read(UUID conversationId, UUID lastDirectMessageId, UUID requesterId) {

        Conversation conversation = getConversationOrThrow(conversationId);
        User requester = getUserOrThrow(requesterId);

        readUnreadMessagesInAndSave(conversation, requester);

        log.info("DM 읽음 처리 완료, directMessageId={}", lastDirectMessageId);
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

    private DirectMessage getDirectMessageOrThrow(UUID directMessageUuid) {
        return directMessageRepository.findByUuid(directMessageUuid)
                .orElseThrow(() -> new DirectMessageNotFoundException(directMessageUuid));
    }

    private void readUnreadMessagesInAndSave(Conversation conversation, User requester) {
        directMessageRepository.readUnreadMessages(conversation.getId(), requester.getId());
    }

    private User getUserOrThrow(UUID senderUuid) {
        return userRepository.findUserAndProfileOnlyByUuid(senderUuid)
                .orElseThrow(() -> new ParticipantNotFoundException(senderUuid));
    }

    private User getCounterpartOrThrow(Conversation conversation, Long senderId) {
        if(!conversation.isValidParticipant(senderId)) throw new ParticipantNotFoundException();
        Long receiverId = conversation.getCounterpartId(senderId);
        return userRepository.findUserAndProfileOnlyById(receiverId)
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
