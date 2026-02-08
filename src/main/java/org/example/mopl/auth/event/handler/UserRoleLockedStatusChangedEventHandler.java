package org.example.mopl.auth.event.handler;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.service.AuthService;
import org.example.mopl.user.event.UserRoleLockStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserRoleLockedStatusChangedEventHandler {

    private final AuthService authService;

    @EventListener(UserRoleLockStatusChangedEvent.class)
    public void onEvent(UserRoleLockStatusChangedEvent event) {
        if(event == null || StringUtils.hasText(event.getUserEmail()) == false)
            return;

        authService.deleteRefreshToken(event.getUserEmail());
    }
}
