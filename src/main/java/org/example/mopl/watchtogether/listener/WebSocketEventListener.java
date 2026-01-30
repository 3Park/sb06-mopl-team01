package org.example.mopl.watchtogether.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WatchTogetherService watchTogetherService;
    //어디에서 컨텐츠 정보를 호출해야 의존성 순환이 안될지 생각중
    //여기에서 컨텐츠 정보를 호츨 해도 될까?

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String contentId = Objects.requireNonNull(headerAccessor.getDestination()).split("/")[3];
        CustomUserDetails details = (CustomUserDetails) headerAccessor.getUser();

        UserDto userDto = null;
        if(details ==null){
            throw new RuntimeException("사용자 없음");
        }
        userDto = details.getUserDto();

        watchTogetherService.addUserToRoom(userDto,contentId,sessionId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        CustomUserDetails details = (CustomUserDetails) headerAccessor.getUser();

        UserDto userDto = null;
        if(details ==null){
            throw new RuntimeException("사용자 없음");
        }
        userDto = details.getUserDto();

        watchTogetherService.removeUserFromRoom(sessionId,userDto);
    }
}
