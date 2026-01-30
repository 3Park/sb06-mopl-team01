package org.example.mopl.auth.dto;

public record JwtTokenDto(
        String accessToken,
        String refreshToken,
        JwtDto jwtDto
) {
}
