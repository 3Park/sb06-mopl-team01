package org.example.mopl.common.jwt.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    public void save(String email, String refreshToken) {
        if(StringUtils.hasText(email) == false || StringUtils.hasText(refreshToken) == false)
            return;

        repository.save(email, refreshToken);
    }

    public String findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void delete(String email) {
        repository.delete(email);
    }

    //2중 로그인 방지 등 토큰 검증
    public boolean validateToken(String email,  String refreshToken)
    {
        String storedToken = findByEmail(email);
        if(StringUtils.hasText(storedToken)
                && StringUtils.hasText(refreshToken)
                && storedToken.equals(refreshToken))
            return true;

        return false;
    }
}
