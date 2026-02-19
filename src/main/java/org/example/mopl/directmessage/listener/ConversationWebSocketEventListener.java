package org.example.mopl.directmessage.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationWebSocketEventListener {

    private final StringRedisTemplate redisTemplate;
    private final Map<String, UUID> sessionToConversationId = new ConcurrentHashMap<>();

    // 채팅방 입장 (구독) 감지
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/sub/conversations")) {
            UUID conversationId = extractConversationId(destination);
            UUID userId = extractUserId(accessor.getUser());

            if (conversationId != null && userId != null) {
                sessionToConversationId.put(sessionId, conversationId);

                String redisKey = "conversation:" + conversationId + ":participants";
                redisTemplate.opsForSet().add(redisKey, userId.toString());

                log.info("채팅방 입장: 유저 {} -> 방 {}", userId, conversationId);
            }
        }
    }

    // 채팅방 퇴장 (연결 끊김) 감지
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        // DisconnectEvent에는 destination 정보 포함되어 있지 않음
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) return;

        UUID conversationId = sessionToConversationId.remove(sessionId);
        UUID userId = extractUserId(accessor.getUser());

        if (conversationId != null && userId != null) {
            String redisKey = "conversation:" + conversationId + ":participants";
            redisTemplate.opsForSet().remove(redisKey, userId.toString());

            log.info("채팅방 퇴장: 유저 {} -> 방 {}", userId, conversationId);
        }
    }

    // 채팅방 구독 취소 (다른 페이지로 이동) 감지
    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        // DisconnectEvent에는 destination 정보 포함되어 있지 않음
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) return;

        UUID conversationId = sessionToConversationId.remove(sessionId);
        UUID userId = extractUserId(accessor.getUser());

        if (conversationId != null && userId != null) {
            String redisKey = "conversation:" + conversationId + ":participants";
            redisTemplate.opsForSet().remove(redisKey, userId.toString());

            log.info("채팅방 구독 취소(나가기): 유저 {} -> 방 {}", userId, conversationId);
        }
    }

    private UUID extractConversationId(String destination) {
        if (destination == null) return null;
        String[] parts = destination.split("/");
        return parts.length >= 4 ? UUID.fromString(parts[3]) : null;
    }
    private UUID extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object detail = authentication.getPrincipal();
            if (detail instanceof CustomUserDetails userDetails) {
                return userDetails.getUserDto().getId();
            }
        }
        return null;
    }
}
