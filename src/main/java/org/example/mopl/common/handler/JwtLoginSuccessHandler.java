package org.example.mopl.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.JwtTokenProvider;
import org.example.mopl.common.jwt.TokenUtils;
import org.example.mopl.common.jwt.service.RefreshTokenService;
import org.example.mopl.user.custom.CustomUserDetails;
import org.example.mopl.user.dto.JwtDto;
import org.example.mopl.user.exception.UserErrorCode;
import org.example.mopl.user.exception.UserException;
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
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        if(user == null || user.getUserDto() == null || StringUtils.isEmpty(user.getUserDto().getEmail())){
            throw new UserException(UserErrorCode.INVALID_USER_DATA);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserDto().getEmail(), user.getUserDto().getRole());
        String refreshToken =  jwtTokenProvider.generateRefreshToken(user.getUserDto().getEmail(), user.getUserDto().getRole());

        response.addCookie(tokenUtils.getRefreshCookie(refreshToken));
        refreshTokenService.save(user.getUserDto().getEmail(), refreshToken);

        JwtDto jwtDto = new JwtDto(user.getUserDto(),accessToken);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    }
}
