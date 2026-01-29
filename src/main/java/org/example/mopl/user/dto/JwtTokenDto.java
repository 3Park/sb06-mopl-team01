package org.example.mopl.user.dto;

public record JwtTokenDto(
        String accessToken,
        String refreshToken,
        JwtDto jwtDto
) {
}
