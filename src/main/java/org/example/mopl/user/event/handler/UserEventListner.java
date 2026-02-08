package org.example.mopl.user.event.handler;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.event.OAuthUserCreateEvent;
import org.example.mopl.auth.event.TemporaryPasswordIssuedEvent;
import org.example.mopl.user.dto.request.UserCreateRequest;
import org.example.mopl.user.service.TemporaryPasswordService;
import org.example.mopl.user.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class UserEventListner {

    private final TemporaryPasswordService temporaryPasswordService;
    private final UserService userService;

    @Async
    @EventListener(TemporaryPasswordIssuedEvent.class)
    public void TemporaryPasswordIssuedEvent(TemporaryPasswordIssuedEvent event) {
        if(event == null
            || StringUtils.hasText(event.getTemporaryPassword()) == false
            ||  StringUtils.hasText(event.getReceiverEmail()) == false)
            return;

        temporaryPasswordService.save(event.getReceiverEmail(), event.getTemporaryPassword());
    }

    //async 안 쓰는 이유 : oauth 인증 성공 후 유저 생성 -> 리다이렉트 하는데, 프론트에서 바로 refresh 를 한다.
    //이때 유저가 없으면 안됨. 
    @EventListener
    public void CreateOAuthUser(OAuthUserCreateEvent event) {
        if(event == null
                || StringUtils.hasText(event.getEmail()) == false
                ||  StringUtils.hasText(event.getName()) == false)
            return;

        UserCreateRequest request = UserCreateRequest.builder()
                .email(event.getEmail())
                .name(event.getName())
                .password("OAuth")
                .build();

        userService.createUser(request);
    }
}
