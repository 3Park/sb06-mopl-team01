package org.example.mopl.auth.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorResponse;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        AuthErrorCode errorCode = AuthErrorCode.INVALID_USER_CREDENTIALS;
        if(exception.getClass().equals(LockedException.class) || exception.getClass().equals(OAuth2AuthenticationException.class)) {
            errorCode = AuthErrorCode.INVALID_USER;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                new AuthException(errorCode)
                , HttpStatus.UNAUTHORIZED.value());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
