package org.example.mopl.auth.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.jwt.TokenUtils;
import org.example.mopl.auth.service.AuthService;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.dto.JwtDto;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final TokenUtils tokenUtils;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        if(user == null || user.getUserDto() == null || StringUtils.isEmpty(user.getUserDto().getEmail())){
            throw new AuthException(AuthErrorCode.INVALID_USER_DATA);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserDto().getEmail(), user.getUserDto().getRole());
        String refreshToken =  jwtTokenProvider.generateRefreshToken(user.getUserDto().getEmail(), user.getUserDto().getRole());

        response.addCookie(tokenUtils.getRefreshCookie(refreshToken));
        authService.saveRefreshToken(user.getUserDto().getEmail(), refreshToken);

        JwtDto jwtDto = new JwtDto(user.getUserDto(),accessToken);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    }
}
