package org.example.mopl.common.interceptor;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.directmessage.repository.ConversationRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ConversationRepository conversationRepository;

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
        Principal user = accessor.getUser();

        if (destination != null && destination.startsWith("/sub/contents")) {
            String roomId = extractContentId(destination);
            if (roomId == null || roomId.trim().isEmpty()) {
                throw new SecurityException("잘못된 구독 경로");
            }
        }
        if (destination != null && destination.startsWith("/sub/conversations")) {
            String conversationId = extractConversationId(destination);
            if (conversationId == null || conversationId.trim().isEmpty()) {
                throw new SecurityException("잘못된 구독 경로");
            }
            if (user == null) {
                throw new SecurityException("로그인 정보가 없습니다.");
            }

            // 해당 채팅방의 참여자가 아닐 시 구독 불가
            UUID conversationUuid = UUID.fromString(conversationId);

            Authentication authentication = (Authentication) user;
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isParticipant = conversationRepository
                    .existsByConversationUuidAndUserUuid(conversationUuid, userDetails.getUserDto().getId());
            if (!isParticipant) {
                throw new SecurityException("채팅방 구독 권한이 없습니다.");
            }
        }

        return message;
    }

    private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        String username = (user != null)? accessor.getUser().getName() : null;

        if (destination == null) {
            return message; // 하트비트 메시지
        }

        if (destination.startsWith("/pub/contents")) {
            String roomId = extractContentId(destination);
            if (roomId == null || username == null) {
                throw new SecurityException("잘못된 메시지 대상");
            }
        }

        if (destination.startsWith("/pub/conversations")) {
            String conversationId = extractConversationId(destination);
            if (conversationId == null || conversationId.trim().isEmpty() || username == null) {
                throw new SecurityException("잘못된 대상입니다.");
            }
        }


        return message;
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        String username = (user != null)? accessor.getUser().getName() : null;

        log.info("STOMP 연결 해제: {}", username);
    }

    private String extractContentId(String destination) {
        // destination: /sub/contents/{contentId}/watch
        if (destination == null) return null;
        String[] parts = destination.split("/");
        return parts.length >= 4 ? parts[2] : null;
    }

    private String extractConversationId(String destination) {
        if (destination == null) return null;
        String[] parts = destination.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
