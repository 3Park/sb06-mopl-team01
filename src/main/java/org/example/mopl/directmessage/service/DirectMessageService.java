package org.example.mopl.directmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.directmessage.dto.*;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.dto.request.ConversationListRequest;
import org.example.mopl.directmessage.dto.request.DirectMessageListRequest;
import org.example.mopl.directmessage.dto.response.CursorResponseConversationDto;
import org.example.mopl.directmessage.dto.data.DirectMessageDto;
import org.example.mopl.directmessage.dto.response.CursorResponseDirectMessageDto;
import org.example.mopl.directmessage.entity.BaseEntity;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.enums.Type;
import org.example.mopl.directmessage.exception.ConversationForbiddenException;
import org.example.mopl.directmessage.exception.ConversationNotFoundException;
import org.example.mopl.directmessage.exception.DirectMessageNotFoundException;
import org.example.mopl.directmessage.exception.ParticipantNotFoundException;
import org.example.mopl.directmessage.repository.ConversationRepository;
import org.example.mopl.directmessage.repository.DirectMessageRepository;
import org.example.mopl.event.message.DmMessageReceivedKafkaEvent;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final StringRedisTemplate redisTemplate;

    // DM 생성 및 전송
    @Transactional
    public void saveAndSendMessage(UUID conversationUuid, UUID senderUuid, String content) {

        Conversation conversation = getConversation(conversationUuid);
        User sender = getUser(senderUuid);

        User receiver = getCounterpartOrThrow(conversation, sender.getId());

        DirectMessage directMessage = saveMessage(conversation, sender, receiver, content);

        updateConversationScoreInCache(conversation, sender, receiver, directMessage);

        log.debug("메시지 저장 완료, messageId={}", directMessage.getUuid());

        sendToSocket(conversationUuid, directMessage, sender, receiver);

        // 상대방이 대화방 접속 중이 아니면 DM 알림 발송
        if (isParticipantOffline(conversation, receiver)) {
            eventPublisher.publishEvent(DmMessageReceivedKafkaEvent.of(
                    receiver.getUuid(), sender.getProfile().getName(), content));
        }

        log.info("메시지 전송 완료, messageId={}", directMessage.getUuid());

    }

    // 대화 생성
    @Transactional
    public ConversationDto create(UUID creatorId, UUID joinId) {

        User creator = getUser(creatorId);
        User joiner = getUser(joinId);

        Conversation conversation = getExistingConversation(creator, joiner)
                .orElseGet(() -> {
                    Conversation conv = saveConversation(creator, joiner);
                    cacheConversationForParticipants(conv, creatorId, joinId);
                    return conv;
                });

        log.info("conversation 생성 완료, conversationId={}", conversation.getUuid());
        return ConversationDto.from(
                conversation, creator, joiner,
                findLastMessage(conversation).orElse(null)
        );
    }

    // DM 읽음 처리
    @Transactional
    public void markAsRead(UUID conversationId, UUID lastDirectMessageId, UUID requesterId) {

        Conversation conversation = getConversation(conversationId);
        User requester = getUser(requesterId);
        DirectMessage lastDirectMessage = getDirectMessage(lastDirectMessageId);

        markAsReadMessages(conversation, requester, lastDirectMessage);

        // 대화방 상대에게 읽은 사실 실시간 전송
        ReadReceiptDto readReceiptDto = ReadReceiptDto.of(
                Type.READ, conversationId, lastDirectMessageId, requesterId);
        messagingTemplate.convertAndSend(resolveDestination(conversationId), readReceiptDto);

        log.info("DM 읽음 처리 완료, directMessageId={}", lastDirectMessageId);
    }

    // 대화 조회
    @Transactional(readOnly = true)
    public ConversationDto get(UUID requesterUuid, UUID conversationUuid) {

        Conversation conversation = getConversation(conversationUuid);
        User requester = getUser(requesterUuid);
        User other = getCounterpartOrThrow(conversation, requester.getId());

        log.info("대화 조회 완료, conversationId={}", conversationUuid);
        return ConversationDto.from(
                conversation, requester, other,
                findLastMessage(conversation).orElse(null)
        );
    }

    // 특정 사용자와의 대화 조회
    @Transactional(readOnly = true)
    public ConversationDto getWith(UUID requesterUuid, UUID withUserUuid) {

        User requester = getUser(requesterUuid);
        User withUser = getUser(withUserUuid);

        Conversation conversation = getExistingConversation(requester, withUser)
                .orElseThrow(() -> new ConversationNotFoundException());


        log.info("with={} 사용자와의 대화 조회 완료, conversationId={}", withUserUuid, conversation.getUuid());
        return ConversationDto.from(
                conversation, requester, withUser,
                findLastMessage(conversation).orElse(null)
        );
    }

    // 대화 목록 조회
    @Transactional(readOnly = true)
    public CursorResponseConversationDto getConversations(UUID requesterUuid, ConversationListRequest request) {

        User requester = getUser(requesterUuid);
        String redisKey = resolveRedisKey(requesterUuid);
        boolean hasKey = redisTemplate.hasKey(redisKey);
        Long maxScore = null;

        if (hasKey) {
            maxScore = resolveMaxScore(request.idAfter(), redisKey);
        }
        List<Conversation> conversations;

        boolean isCacheMiss = (!hasKey || maxScore == null);

        ConversationSearchCondition condition = request.toSearchCondition(
                requester.getId(), request.limit() + 1
        );
        Long totalCount = conversationRepository.countByCursor(condition);

        if (!isCacheMiss) {
            conversations = fetchConversationsFromRedis(redisKey, maxScore, request.limit() + 1);
        } else {
            conversations = conversationRepository.searchByCursor(condition);
        }

        // 어플리케이션 조인 (상대방, 대화방의 마지막 메시지)
        Map<Long, User> counterpartMap = findCounterpartUserByConversations(requester, conversations);
        Map<Long, DirectMessage> lastMessageMap = findLastMessagesByConversations(conversations);

        CursorResult<Conversation> cursorResult = getCursorResult(conversations, request.limit());
        List<ConversationDto> data = toConversationDtos(
                cursorResult.items(), requester, counterpartMap, lastMessageMap
        );

        if (isCacheMiss) { cacheConversationsToRedis(redisKey, conversations, data); }

        return CursorResponseConversationDto.builder()
                .data(data)
                .nextCursor(cursorResult.nextCursor()).nextIdAfter(cursorResult.nextIdAfter())
                .hasNext(cursorResult.hasNext()).totalCount(totalCount)
                .sortBy(request.sortBy()) // 현재 sortBy 파라미터 무시하고 최신순으로 정렬 중
                .sortDirection(request.sortDirection())
                .build();
    }

    // DM 목록 조회
    @Transactional(readOnly = true)
    public CursorResponseDirectMessageDto getDirectMessages(
            UUID requesterUuid, UUID conversationUuid, DirectMessageListRequest request) {

        User requester = getUser(requesterUuid);
        Conversation conversation = getConversation(conversationUuid);
        User other =  getCounterpartOrThrow(conversation, requester.getId());

        DirectMessageSearchCondition condition = request.toSearchCondition(
                requester.getId(), conversation.getId(), request.limit() + 1
        );

        List<DirectMessage> directMessages = directMessageRepository.searchByCursor(condition);
        Long totalCount = directMessageRepository.countByCursor(condition);

        CursorResult<DirectMessage> cursorResult = getCursorResult(directMessages, request.limit());
        List<DirectMessageDto> data = toDirectMessageDtos(cursorResult.items(), requester, other);

        log.info("대화 목록 조회 완료, requesterId={}, conversationId={}", requesterUuid, conversationUuid);
        return CursorResponseDirectMessageDto.builder()
                .data(data)
                .nextCursor(cursorResult.nextCursor()).nextIdAfter(cursorResult.nextIdAfter())
                .hasNext(cursorResult.hasNext()).totalCount(totalCount)
                .sortBy(request.sortBy()).sortDirection(request.sortDirection())
                .build();
    }

    // DM 대화방 내 상대 타이핑 여부 전송
    @Transactional
    public void sendTypingEvent(UUID conversationId, UUID senderId, boolean isTyping) {

        TypingDto dto = TypingDto.of(conversationId, senderId, isTyping);
        messagingTemplate.convertAndSend(resolveDestination(conversationId), dto);
    }

    // ===== helper method =====

    private User getUser(UUID senderUuid) {
        return userRepository.findUserAndProfileOnlyByUuid(senderUuid)
                .orElseThrow(() -> new ParticipantNotFoundException(senderUuid));
    }
    private Conversation getConversation(UUID conversationUuid) {
        return conversationRepository.findByUuid(conversationUuid)
                .orElseThrow(() -> new ConversationNotFoundException(conversationUuid));
    }
    private DirectMessage getDirectMessage(UUID directMessageUuid) {
        return directMessageRepository.findByUuid(directMessageUuid)
                .orElseThrow(() -> new DirectMessageNotFoundException(directMessageUuid));
    }
    private User getCounterpartOrThrow(Conversation conversation, Long requesterId) {
        if(!conversation.isValidParticipant(requesterId)) {
            throw new ConversationForbiddenException(conversation.getUuid());
        }
        Long counterpartId = conversation.getCounterpartId(requesterId);
        return userRepository.findUserAndProfileOnlyById(counterpartId)
                .orElseThrow(() -> new ParticipantNotFoundException());
    }
    private Optional<Conversation> getExistingConversation(User user1, User user2) {
        return conversationRepository.findExisting(user1.getId(), user2.getId());
    }
    private Optional<DirectMessage> findLastMessage(Conversation conversation) {
        return directMessageRepository
                .findFirstByConversationIdOrderByIdDesc(conversation.getId());
    }

    // 객체 생성 후 save -> return
    private DirectMessage saveMessage(Conversation conversation, User sender, User receiver, String content) {
        DirectMessage directMessage = DirectMessage.of(conversation, sender.getId(), receiver.getId(), content);
        return directMessageRepository.save(directMessage);
    }
    private Conversation saveConversation(User creator, User joiner) {
        Conversation conversation = Conversation.of(creator.getId(), joiner.getId());
        return conversationRepository.save(conversation);
    }

    // Bulk fetch method (N+1 방지)
    // Conversation별 상대 유저 일괄 조회
    private Map<Long, User> findCounterpartUserByConversations(User requester, List<Conversation> conversations) {
        List<Long> counterpartIds = conversations.stream()
                .map(c -> c.getCounterpartId(requester.getId())).toList();
        return userRepository.findAllWithProfileByIdIn(counterpartIds).stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user));
    }
    // Conversation별 마지막 메시지 일괄 조회
    private Map<Long, DirectMessage> findLastMessagesByConversations(List<Conversation> conversations) {
        List<Long> conversationIds = conversations.stream().map(Conversation::getId).toList();
        return directMessageRepository.findAllLastMessagesByConversationIdIn(conversationIds);
    }

    // List 단위 dto 변환
    private List<ConversationDto> toConversationDtos(
            List<Conversation> conversations, User requester,
            Map<Long, User> counterpartMap, Map<Long, DirectMessage> lastMessageMap
    ) {
        return conversations.stream()
                .map(conversation -> {
                    Long otherId = conversation.getCounterpartId(requester.getId());
                    User other = counterpartMap.get(otherId);
                    DirectMessage lastMessage = lastMessageMap.get(conversation.getId());
                    return ConversationDto.from(conversation, requester, other, lastMessage);
                }).toList();
    }
    private List<DirectMessageDto> toDirectMessageDtos(
            List<DirectMessage> directMessages, User requester, User other
    ) {
        return directMessages.stream()
                .map(directMessage -> {
                    boolean isMe = directMessage.isSenderId(requester.getId());
                    User sender = isMe? requester : other;
                    User receiver = isMe? other : requester;
                    return DirectMessageDto.from(directMessage, sender, receiver);
                })
                .toList();
    }

    // 읽지 않은 메시지 중 lastMessage 이하인 것들을 일괄 읽음 처리
    private void markAsReadMessages(Conversation conversation, User requester, DirectMessage directMessage) {
        directMessageRepository.markAsRead(conversation.getId(), requester.getId(), directMessage.getId());
    }
    private void sendToSocket(UUID conversationUuid, DirectMessage directMessage, User sender, User receiver) {
        DirectMessageDto dto = DirectMessageDto.from(directMessage, sender, receiver);
        messagingTemplate.convertAndSend(resolveDestination(conversationUuid), dto);
    }
    private String resolveDestination(UUID conversationUuid) {
        return "/sub/conversations/" + conversationUuid + "/direct-messages";
    }
    // DM - 상대방의 현재 대화방 접속 여부 확인
    private boolean isParticipantOffline(Conversation conversation, User user) {
        String redisKey = "conversation:" + conversation.getUuid() + ":participants";
        return Boolean.FALSE.equals(
                redisTemplate.opsForSet().isMember(redisKey, user.getUuid().toString()));
    }
    private <T extends BaseEntity> CursorResult<T> getCursorResult(
            List<T> items, int limit) {
        boolean hasNext = false;
        String nextCursor = null;
        UUID nextIdAfter = null;
        List<T> itemsAfter = items;

        if (itemsAfter.size() > limit) {
            hasNext = true;
            itemsAfter = itemsAfter.subList(0, limit);
        }
        if (!itemsAfter.isEmpty()) {
            nextIdAfter = itemsAfter.get(itemsAfter.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }
        return new CursorResult<>(
                itemsAfter, hasNext, nextCursor, nextIdAfter
        );
    }
    private long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Redis Caching
    private String resolveRedisKey(UUID userId) {
        return "user:" + userId + ":conversations";
    }
    private Long resolveMaxScore(UUID conversationId, String redisKey) {
        // idAfter 파라미터 없으면 지금 시간 (첫 페이지)
        if (conversationId == null) {
            return System.currentTimeMillis();
        }
        Double cursorScore = redisTemplate.opsForZSet().score(redisKey, conversationId.toString());

        if (cursorScore != null) {
            return cursorScore.longValue() - 1;
        } else {
            return null;
        }
    }
    private List<Conversation> fetchConversationsFromRedis(String redisKey, Long maxScore, int limit) {
        Set<String> conversationIdsStr = Optional.ofNullable(redisTemplate.opsForZSet()
                .reverseRangeByScore(redisKey, 0, maxScore, 0, limit)
        ).orElse(Collections.emptySet());

        if (conversationIdsStr.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> conversationIds = conversationIdsStr.stream().map(UUID::fromString).toList();

        // IN 쿼리 조회 -> 정렬되지 않은 리스트
        List<Conversation> unorderedConversations = conversationRepository.findByUuidIn(conversationIds);
        // 재정렬
        Map<UUID, Conversation> conversationMap = unorderedConversations.stream()
                .collect(Collectors.toMap(c -> c.getUuid(), c -> c));

        return conversationIds.stream()
                .map(c -> conversationMap.get(c))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    private void cacheConversationsToRedis(String redisKey,
                                           List<Conversation> conversations,
                                           List<ConversationDto> conversationDtos) {
        for (int i = 0; i < conversationDtos.size(); i++) {
            // 마지막 메시지 존재? 메시지 생성 시각 : 대화방 생성 시각
            long score = (conversationDtos.get(i).lastestMessage() != null)
                    ? toEpochMilli(conversationDtos.get(i).lastestMessage().createdAt())
                    : toEpochMilli(conversations.get(i).getCreatedAt());
            redisTemplate.opsForZSet().add(redisKey, conversationDtos.get(i).id().toString(), score);
        }
        redisTemplate.expire(redisKey, Duration.ofHours(12));
    }
    private void cacheConversationForParticipants(Conversation conversation, UUID creatorId, UUID joinId) {
        redisTemplate.opsForZSet().add(resolveRedisKey(creatorId),
                conversation.getUuid().toString(), toEpochMilli(conversation.getCreatedAt()));
        redisTemplate.opsForZSet().add(resolveRedisKey(joinId),
                conversation.getUuid().toString(), toEpochMilli(conversation.getCreatedAt()));
    }
    private void updateConversationScoreInCache(Conversation conversation, User sender,
                                                User receiver, DirectMessage message) {
        redisTemplate.opsForZSet().add(resolveRedisKey(sender.getUuid()),
                conversation.getUuid().toString(), toEpochMilli(message.getCreatedAt()));
        redisTemplate.opsForZSet().add(resolveRedisKey(receiver.getUuid()),
                conversation.getUuid().toString(), toEpochMilli(message.getCreatedAt()));
    }
}
