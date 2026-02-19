package org.example.mopl.auth.jwt.handler;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.jwt.TokenUtils;
import org.example.mopl.user.enums.UserRoleType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();

        if(user == null || user.getAttributes() == null || user.getAttributes().isEmpty())
            throw new AuthException(AuthErrorCode.INVALID_USER_DATA);

        String email = user.getAttribute("email").toString();
        String role = UserRoleType.USER.name();

        String refreshToken =  jwtTokenProvider.generateRefreshToken(email, role);

        response.addCookie(tokenUtils.getRefreshCookie(refreshToken));

        response.sendRedirect("/index.html");
    }
}
