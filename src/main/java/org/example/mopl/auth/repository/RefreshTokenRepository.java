package org.example.mopl.auth.repository;

import lombok.RequiredArgsConstructor;
import org.example.mopl.common.config.property.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(String email, String refreshToken) {
        redisTemplate.opsForValue().set(email,
                refreshToken,
                jwtProperties.getRefreshKeyExpiration(),
                TimeUnit.MILLISECONDS);
    }

    public String findByEmail(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public void delete(String email) {
        redisTemplate.delete(email);
    }
}
