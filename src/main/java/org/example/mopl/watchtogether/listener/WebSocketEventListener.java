package org.example.mopl.watchtogether.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.example.mopl.content.entity.Content;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ConcurrentHashMap<String,String> sessionToDestination = new ConcurrentHashMap<>();

    private final WatchTogetherService watchTogetherService;

    private final String CONTENTS = "/sub/contents";

    //공통적인 값 의논하고 리팩토링 진행 예정
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        //sessionId,구독한 채널 주소, roomId(DM방,컨텐츠방), 유저정보 세션에서 추출
        String sessionId = headerAccessor.getSessionId();

        //sub/conversations/{conversationId},/pub/contents/{contentId} 해당 아이디 추출
        String roomId = Objects.requireNonNull(headerAccessor.getDestination()).split("/")[3];

        //채널별로 이벤트 처리 달리하기 위해 추출
        String destination = headerAccessor.getDestination();

        //세션 연결 종료 때 채널 주소 이용
        //세션이 null인 경우가 있나 아니면 그냥 ConcurrentHashMap 입력 값이 @NotNull 이라서
        //headerAccessor.getSessionId() 여기에서 불변으로 반환을 안해서 경고가 뜨는 것 같다.
        sessionToDestination.put(sessionId,destination);

        //JWT에서 유저정보 추출
        CustomUserDetails details = (CustomUserDetails) headerAccessor.getUser();
        UserDto userDto = null;
        if(details ==null){
            throw new AuthException(AuthErrorCode.INVALID_USER_DATA);
        }
        userDto = details.getUserDto();

        if(destination.startsWith(CONTENTS)){
            watchTogetherService.addUserToRoom(userDto,roomId,sessionId);
        }

    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionToDestination.get(sessionId).startsWith(CONTENTS)){
            watchTogetherService.removeUserFromRoom(sessionId);
        }

    }
}
