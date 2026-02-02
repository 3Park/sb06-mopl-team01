package org.example.mopl.auth.port.adapter;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.port.AuthPort;
import org.example.mopl.user.service.TemporaryPasswordService;
import org.example.mopl.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthAdapter implements AuthPort {

    private final UserService userService;
    private final TemporaryPasswordService temporaryPasswordService;

    @Override
    public boolean invalidEmail(String email) {
        if(StringUtils.hasText(email)
            && userService.existsUserByEmail(email)
        )
            return false;

        return true;
    }
}
