package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.port.adapter.AuthAdapter;
import org.example.mopl.auth.repository.RefreshTokenRepository;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.dto.JwtDto;
import org.example.mopl.auth.dto.JwtTokenDto;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository repository;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenDto tokenRotate(String refreshToken)
    {
        Authentication auth = jwtTokenProvider.getAuthentication(refreshToken);
        if (auth == null)
            throw new AuthException(AuthErrorCode.INVALID_USER_CREDENTIALS);

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        if(userDetails == null
                || userDetails.getUserDto() == null
                || StringUtils.hasText(userDetails.getUserDto().getEmail()) == false)
            throw new AuthException(AuthErrorCode.INVALID_USER_DATA);

        String userEmail = userDetails.getUserDto().getEmail();
        String userRole = userDetails.getUserDto().getRole();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userEmail, userRole);
        String newRefreshToken = jwtTokenProvider.generateAccessToken(userEmail, userRole);

        repository.save(userEmail, newRefreshToken);

        return new JwtTokenDto(
                newAccessToken,
                newRefreshToken,
                new JwtDto(
                    userDetails.getUserDto(),
                    newAccessToken
                )
        );
    }

    public void saveRefreshToken(String email, String refreshToken) {
        if(StringUtils.hasText(email) == false || StringUtils.hasText(refreshToken) == false)
            return;

        repository.save(email, refreshToken);
    }

    public String findRefreshTokenByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void deleteRefreshToken(String email) {
        repository.delete(email);
    }

    //2중 로그인 방지 등 토큰 검증
    public boolean validateToken(String email,  String refreshToken)
    {
        String storedToken = findRefreshTokenByEmail(email);
        if(StringUtils.hasText(storedToken)
                && StringUtils.hasText(refreshToken)
                && storedToken.equals(refreshToken))
            return true;

        return false;
    }
}
