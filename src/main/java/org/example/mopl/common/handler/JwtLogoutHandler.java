package org.example.mopl.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.TokenUtils;
import org.example.mopl.common.jwt.service.RefreshTokenService;
import org.example.mopl.user.custom.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;
    private final TokenUtils tokenUtils;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(TokenUtils.REFRESH_TOKEN))
                .findFirst()
                .ifPresent(cookie -> {
                    CustomUserDetails details = (CustomUserDetails)authentication.getPrincipal();
                    if(details != null
                            && details.getUserDto() != null
                            && StringUtils.isEmpty(details.getUserDto().getEmail()) == false){
                        refreshTokenService.delete(details.getUserDto().getEmail());
                    }
                });

        response.addCookie(tokenUtils.emptyRefreshCookie());
    }
}
