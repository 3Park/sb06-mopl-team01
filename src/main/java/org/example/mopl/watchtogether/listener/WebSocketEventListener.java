package org.example.mopl.watchtogether.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ConcurrentHashMap<String,String> sessionToDestination = new ConcurrentHashMap<>();
    private final WatchTogetherService watchTogetherService;

    private final String CONTENTS = "/sub/contents";

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        //1. 유효성 검사 (세션, 목적지, 유저 정보)
        if (sessionId == null || destination == null) {return;}

        UserDto userDto = extractUserDto(accessor.getUser());

        // putIfAbsent를 사용하여 원자적으로(Atomic) 중복 체크
        if (sessionToDestination.putIfAbsent(sessionId, destination) != null) {
            log.info("이미 처리 중인 구독 세션입니다. 중복 무시: {}", sessionId);
            return;
        }

        if (userDto == null) {
            log.warn("인증되지 않은 사용자의 구독 시도입니다. SessionID: {}", sessionId);
            // 필요시 여기서 예외를 던지거나 연결을 끊을 수 있음
            return;
        }

        //2. 연결 해제 시를 대비해 매핑 정보 저장
        sessionToDestination.put(sessionId, destination);

        //3. 콘텐츠 방 구독 로직 처리
        if (destination.startsWith(CONTENTS)) {
            String contentId = extractContentId(destination);
            if (contentId != null) {
                watchTogetherService.addUserToRoom(userDto, contentId, sessionId);
                log.info("User {} entered room {}", userDto.getId(), contentId);
            } else {
                log.warn("잘못된 구독 경로입니다: {}", destination);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) return;

        removeSession(sessionId);
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) return;

        removeSession(sessionId);
    }

    private UserDto extractUserDto(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object detail = authentication.getPrincipal();
            if (detail instanceof CustomUserDetails userDetails) {
                return userDetails.getUserDto();
            }
        }
        return null;
    }

    private String extractContentId(String destination) {
        if (destination == null) {return null;}

        String[] pathParts = destination.split("/");

        //구조 검증: ["", "sub", "contents", "{contentId}"] -> 길이 4 이상이어야 함
        if (pathParts.length > 3) {return pathParts[3];}

        return null;
    }

    private void removeSession(String sessionId){
        String destination = sessionToDestination.remove(sessionId);

        if (destination != null && destination.startsWith(CONTENTS)) {
            watchTogetherService.removeUserFromRoom(sessionId);
            log.info("Session Remove: {}", sessionId);
        }
    }
}
