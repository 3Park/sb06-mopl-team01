package org.example.mopl.directmessage.dto.data;

import org.example.mopl.user.entity.User;

import java.util.UUID;

public record SimpleUserDto(
        UUID userId,
        String name,
        String profileImageUrl
) {
    public static SimpleUserDto from(User user) {
        return new SimpleUserDto(
                user.getUuid(),
                user.getProfile().getName(),
                user.getProfile().getProfileImageUrl()
        );
    }
}
