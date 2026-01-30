package org.example.mopl.auth.dto;

import org.example.mopl.user.dto.UserDto;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
