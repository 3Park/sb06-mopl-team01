package org.example.mopl.auth.jwt.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.jwt.TokenUtils;
import org.example.mopl.auth.service.AuthService;
import org.example.mopl.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final AuthService authService;
    private final TokenUtils tokenUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(TokenUtils.REFRESH_TOKEN))
                .findFirst()
                .ifPresent(cookie -> {
                    //logouthandler가 JWT 필터보다 먼저 호출되어 authentication 정보가 항상 null임.(필터 호출 순서)
                    String accessToken = tokenUtils.getTokenFromRequest(request);
                    Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
                    CustomUserDetails details = (CustomUserDetails)auth.getPrincipal();
                    if(details != null
                            && details.getUserDto() != null
                            && StringUtils.isEmpty(details.getUserDto().getEmail()) == false){
                        authService.deleteRefreshToken(details.getUserDto().getEmail());
                    }
                });

        response.addCookie(tokenUtils.emptyRefreshCookie());
    }
}
