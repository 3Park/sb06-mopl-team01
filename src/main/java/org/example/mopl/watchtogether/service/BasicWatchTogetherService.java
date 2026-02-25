package org.example.mopl.watchtogether.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.common.exception.MoplException;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.ContentErrorCode;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.event.message.WatchTogetherStartKafkaEvent;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.*;
import org.example.mopl.watchtogether.enumeration.ChangeType;
import org.example.mopl.watchtogether.exception.WatchTogetherErrorCode;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicWatchTogetherService implements WatchTogetherService {

    private final RedisTemplate<String, Object> watchTogetherRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ContentCommandRepository contentCommandRepository;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String KEY_ROOM_WATCHERS = "room:%s:watchers";
    private static final String KEY_SESSION_DATA = "session:%s";
    private static final String KEY_SESSION_ROOM = "session:%s:room";
    private static final String KEY_USER_SESSION = "user:%s";
    private static final String KEY_ACTIVE_ROOMS = "active_rooms";

    private static final String ASCENDING = "ASCENDING";
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    @Override
    @Transactional(readOnly = true)
    public void addUserToRoom(UserDto userDto, String contentId, String sessionId) {
        String userKey = String.format(KEY_USER_SESSION, userDto.getId().toString());
        String oldSessionId = stringRedisTemplate.opsForValue().get(userKey);
        String roomKey = String.format(KEY_ROOM_WATCHERS, contentId);

        // 해당 실시간 컨텐츠가 있는지 확인
        Content content =contentCommandRepository.findByUuid(UUID.fromString(contentId))
                .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

        // 기존 세션이 존재하고, 현재 들어온 세션과 다르다면? -> 기존 세션 강제 퇴장 처리
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            log.info("중복 로그인 감지! 기존 세션 정리: userId={}, oldSessionId={}", userDto.getId(), oldSessionId);
            removeUserFromRoom(oldSessionId);
        }

        // 방 활성 목록 관리는 문자열이므로 stringRedisTemplate 사용
        if (!stringRedisTemplate.hasKey(roomKey)) {
            stringRedisTemplate.opsForSet().add(KEY_ACTIVE_ROOMS, contentId);
        }

        Watcher watcher = new Watcher(userDto);
        WatchingSession session = new WatchingSession(sessionId, watcher);

        saveSessionToRedis(sessionId, session, contentId, userKey, roomKey);

        Long count = stringRedisTemplate.opsForZSet().zCard(roomKey);

        // 실시간 같이 보기방 입장 응답 발송
        sendWatchingSessionChangeToRoom(session, content, count != null ? count : 0, ChangeType.JOIN);

        // 나를 팔로우한 사용자에게 알람발송
        notifyFollowedUserActivity(userDto.getId(),userDto.getName(),content.getTitle());
    }

    @Override
    public void removeUserFromRoom(String sessionId) {
        String sessionKey = String.format(KEY_SESSION_DATA, sessionId);
        String sessionRoomKey = String.format(KEY_SESSION_ROOM, sessionId);

        WatchingSession session = (WatchingSession) watchTogetherRedisTemplate.opsForValue().get(sessionKey);
        // 문자열 조회는 stringRedisTemplate
        String contentId = stringRedisTemplate.opsForValue().get(sessionRoomKey);

        if (session != null && contentId != null) {
            String roomKey = String.format(KEY_ROOM_WATCHERS, contentId);
            String userKey = String.format(KEY_USER_SESSION, session.getWatcher().getUserId().toString());

            // ZSet 삭제도 stringRedisTemplate
            stringRedisTemplate.opsForZSet().remove(roomKey, sessionId);

            watchTogetherRedisTemplate.delete(sessionKey);
            stringRedisTemplate.delete(Arrays.asList(sessionRoomKey, userKey));

            Long watcherCount = stringRedisTemplate.opsForZSet().zCard(roomKey);

            if (watcherCount == null || watcherCount == 0) {
                stringRedisTemplate.opsForSet().remove(KEY_ACTIVE_ROOMS, contentId);
                log.info("사용자 없는 방 삭제 roomId: {}", contentId);
            }

            contentCommandRepository.findByUuid(UUID.fromString(contentId)).ifPresent(content ->
                    sendWatchingSessionChangeToRoom(session, content, watcherCount != null ? watcherCount : 0, ChangeType.LEAVE)
            );
            log.info("방에서 사용자 나감 roomId:{} WatcherId: {}", contentId, session.getWatcher().getUserId());
        }
    }

    @Override
    public void sendMessageToRoom(String contentId, ContentChatSendRequest message, Watcher watcher) {
        String roomKey = String.format(KEY_ROOM_WATCHERS, contentId);
        if (stringRedisTemplate.hasKey(roomKey)) {
            String destination = "/sub/contents/" + contentId + "/chat";
            ContentChatDto contentChatDto = new ContentChatDto(watcher, message.content());
            messagingTemplate.convertAndSend(destination, contentChatDto);
        }
    }

    @Override
    public long getWatcherCount(String contentId) {
        if (contentId == null || contentId.isEmpty()) return 0L;
        Long count = stringRedisTemplate.opsForZSet().zCard(String.format(KEY_ROOM_WATCHERS, contentId));
        System.out.println("WatcherCount"+count);
        return count != null ? count : 0L;
    }

    @Override
    public HashMap<Long, Long> getWatchingRooms() {
        // 활성 방 목록도 String Set이므로 stringRedisTemplate 사용
        Set<String> activeRooms = stringRedisTemplate.opsForSet().members(KEY_ACTIVE_ROOMS);
        HashMap<Long, Long> result = new HashMap<>();

        if (activeRooms != null) {
            for (String contentUuid : activeRooms) {
                Content content = contentCommandRepository.findByUuid(UUID.fromString(contentUuid)).orElse(null);
                if (content != null) {
                    Long count = getWatcherCount(contentUuid);
                    System.out.println("WatcherCount"+count);
                    result.put(content.getId(), count);
                }
            }
        }
        return result;
    }

    @Override
    public WatchingSessionDto getWatcher(String watcherId) {
        String userKey = String.format(KEY_USER_SESSION, watcherId);
        String sessionId = stringRedisTemplate.opsForValue().get(userKey); // String 바로 반환

        if (sessionId == null) throw new MoplException(WatchTogetherErrorCode.NO_VIEWERS);

        String sessionKey = String.format(KEY_SESSION_DATA, sessionId);
        String sessionRoomKey = String.format(KEY_SESSION_ROOM, sessionId);

        WatchingSession session = (WatchingSession) watchTogetherRedisTemplate.opsForValue().get(sessionKey);
        String contentId = stringRedisTemplate.opsForValue().get(sessionRoomKey);

        if (session == null || contentId == null) throw new MoplException(WatchTogetherErrorCode.NO_VIEWERS);

        Content content = contentCommandRepository.findByUuid(UUID.fromString(contentId))
                .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

        return WatchingSessionDto.builder()
                .id(contentId)
                .createdAt(session.getCreatedAt())
                .watcher(session.getWatcher())
                .content(toDto(content))
                .build();
    }

    @Override
    public CursorResponseWatchingSessionDto getWatcherList(
            String contentId,
            String watcherNameLike,
            String cursor,
            String idAfter,
            Integer limit,
            String sortDirection,
            String sortBy
    ) {
        Content content = contentCommandRepository.findByUuid(UUID.fromString(contentId))
                .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

        String roomKey = String.format(KEY_ROOM_WATCHERS, contentId);
        String userKey = String.format(KEY_USER_SESSION,idAfter);

        long totalCount = getWatcherCount(contentId);

        List<String> sessionIds = filterSessionIds(roomKey,userKey,limit,sortDirection);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return CursorResponseWatchingSessionDto.toDto(Collections.emptyList(), limit, totalCount, sortBy, sortDirection);
        }

        List<WatchingSession> sessions = getWatchingSessionsByIds(sessionIds);

        List<WatchingSessionDto> data = sessions.stream()
                .map(watchingSession -> new WatchingSessionDto(watchingSession,toDto(content)))
                .toList();

        return CursorResponseWatchingSessionDto.toDto(data, limit, totalCount, sortBy, sortDirection);
    }

    private void notifyFollowedUserActivity(UUID userUuid, String userName, String contentName) {
        List<UUID> followees = followRepository.findAllByFolloweeUuidWithFollower(userUuid).
                stream()
                .map(follow -> follow.getFollower().getUuid())
                .toList();

        applicationEventPublisher.publishEvent(WatchTogetherStartKafkaEvent.of(followees,userName,contentName));
    }

    private void saveSessionToRedis(
            String sessionId,
            WatchingSession session,
            String contentId,
            String userKey,
            String roomKey
    ) {
        // 각종 Key 생성
        String sessionKey = String.format(KEY_SESSION_DATA, sessionId);
        String sessionRoomKey = String.format(KEY_SESSION_ROOM, sessionId);

        // 세션 상세 정보 저장
        watchTogetherRedisTemplate.opsForValue().set(sessionKey, session, SESSION_TTL);

        // 매핑 정보 저장 Session->Room, User->Session
        stringRedisTemplate.opsForValue().set(sessionRoomKey, contentId, SESSION_TTL);
        stringRedisTemplate.opsForValue().set(userKey, sessionId, SESSION_TTL);

        // 방 명단에 추가 - StringRedisTemplate 사용
        stringRedisTemplate.opsForZSet().add(roomKey, sessionId, session.getCreatedAt().toEpochMilli());
    }

    private List<WatchingSession> getWatchingSessionsByIds(List<String> sessionIds) {
        List<String> keys = sessionIds.stream()
                .map(id -> String.format(KEY_SESSION_DATA, id))
                .collect(Collectors.toList());

        // MultiGet은 여전히 JSON 객체를 가져와야 하므로 redisTemplate 사용
        List<Object> sessionObjects = watchTogetherRedisTemplate.opsForValue().multiGet(keys);

        List<WatchingSession> sessions = new ArrayList<>();
        if (sessionObjects != null) {
            for (Object obj : sessionObjects) {
                if (obj != null) {
                    sessions.add((WatchingSession) obj);
                }
            }
        }
        return sessions;
    }

    private void sendWatchingSessionChangeToRoom(
            WatchingSession watcher,
            Content content,
            long WatcherCount,
            ChangeType type) {
        WatchingSessionDto watchingSessionDto = new WatchingSessionDto(watcher, toDto(content));
        WatchingSessionChange message = WatchingSessionChange.builder()
                .type(type)
                .watchingSession(watchingSessionDto)
                .watcherCount(WatcherCount)
                .build();
        String destination = "/sub/contents/" + content.getUuid() + "/watch";
        messagingTemplate.convertAndSend(destination, message);
    }

    private List<String> filterSessionIds(
        String roomKey,
        String userKey,
        Integer limit,
        String sortDirection
    ) {
        Set<String> sessionIds;

        String sessionId = stringRedisTemplate.opsForValue().get(userKey);

        if (ASCENDING.equals(sortDirection)) {
            sessionIds = stringRedisTemplate.opsForZSet().range(roomKey, 0, -1);
        } else {
            sessionIds = stringRedisTemplate.opsForZSet().reverseRange(roomKey, 0, -1);
        }

        if(sessionIds == null) return null;

        if(sessionId != null){
            return sessionIds.stream()
                    .dropWhile( id-> !Objects.equals(id,sessionId))
                    .limit(limit+1)
                    .collect(Collectors.toList());
        }else {
            return sessionIds.stream()
                    .limit(limit+1)
                    .collect(Collectors.toList());
        }

    }

    private ContentDto toDto( Content content ){
        return ContentDto.of(
                content.getUuid(),
                content.getContentType().getValue(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailUrl(),
                null,
                0.0,
                0L,
                0L
        );
    }
}