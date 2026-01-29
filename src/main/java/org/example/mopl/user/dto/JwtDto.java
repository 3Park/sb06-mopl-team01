package org.example.mopl.user.dto;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
