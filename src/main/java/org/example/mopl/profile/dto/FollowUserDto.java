package org.example.mopl.profile.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowUserDto {

    private final Long userId;
    private final UUID userUuid;
    private final String name;
    private final String profileImageUrl;
}
