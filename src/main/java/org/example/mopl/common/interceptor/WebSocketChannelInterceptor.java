package org.example.mopl.common.interceptor;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor != null){

            StompCommand command = accessor.getCommand();

            if(command == null){
                return message;
            }

            try{
                switch (command){
                    case CONNECT:
                        return handleConnect(accessor, message);
                    case SUBSCRIBE:
                        return handleSubscribe(accessor, message);
                    case SEND:
                        return handleSend(accessor,message);
                    case DISCONNECT:
                        handleDisconnect(accessor);
                        break;
                    default:
                        return message;
                }
            }catch (SecurityException e){
                log.error("보안 위반 - 메시지 차단: ",e);
                return null;
            }
        }

        return message;
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String authorization = (String) accessor.getFirstNativeHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("인증되지 않은 연결 시도");
        }

        String jtw = authorization.substring(7);

        Authentication authentication = jwtTokenProvider.getAuthentication(jtw);
        accessor.setUser(authentication);
        return message;
    }

    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        if (destination != null && destination.startsWith("/sub/contents")) {
            String roomId = extractContentId(destination);
            if (roomId == null || roomId.trim().isEmpty()) {
                throw new SecurityException("잘못된 구독 경로");
            }
        }
        return message;
    }

    private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        String username = (String) accessor.getSessionAttributes().get("username");

        if (destination == null) {
            return message; // 하트비트 메시지
        }

        if (destination.startsWith("/pub/contents")) {
            String roomId = extractContentId(destination);
            if (roomId == null || username == null) {
                throw new SecurityException("잘못된 메시지 대상");
            }
        }

        return message;
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String username = (String) accessor.getSessionAttributes().get("username");
        log.info("STOMP 연결 해제: {}", username);
    }

    private String extractContentId(String destination) {
        // destination: /sub/contents/{contentId}/watch
        if (destination == null) return null;
        String[] parts = destination.split("/");
        return parts.length >= 4 ? parts[2] : null;
    }
}
