package org.example.mopl.common.jwt.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.JwtTokenProvider;
import org.example.mopl.common.jwt.repository.RefreshTokenRepository;
import org.example.mopl.user.custom.CustomUserDetails;
import org.example.mopl.user.dto.JwtDto;
import org.example.mopl.user.dto.JwtTokenDto;
import org.example.mopl.user.exception.UserErrorCode;
import org.example.mopl.user.exception.UserException;
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
            throw new UserException(UserErrorCode.INVALID_USER_CREDENTIALS);

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        if(userDetails == null
                || userDetails.getUserDto() == null
                || StringUtils.hasText(userDetails.getUserDto().getEmail()) == false)
            throw new UserException(UserErrorCode.INVALID_USER_DATA);

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
