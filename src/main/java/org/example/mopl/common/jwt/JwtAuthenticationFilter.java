package org.example.mopl.common.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.service.AuthService;
import org.example.mopl.user.custom.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenUtils tokenUtils;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = tokenUtils.getTokenFromRequest(request);
        if(token != null && jwtTokenProvider.validateToken(token)){

            //중복 로그인 방지 (새로 로그인시 기존 유저 접속 차단)
            //Redis 에 저장된 refreshToken과 현재 요청의 refreshToken이 맞지않으면 old 유저.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(customUserDetails != null &&
                    customUserDetails.getUserDto() != null &&
                    StringUtils.hasText(customUserDetails.getUserDto().getEmail())){

                Arrays.stream(request.getCookies())
                        .filter(cookie -> cookie.getName().equals(TokenUtils.REFRESH_TOKEN))
                        .findFirst()
                        .ifPresent(cookie -> {
                            if(authService.validateToken(customUserDetails.getUserDto().getEmail(),
                                    cookie.getValue())){
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        });
            }
        }

        filterChain.doFilter(request, response);
    }
}
