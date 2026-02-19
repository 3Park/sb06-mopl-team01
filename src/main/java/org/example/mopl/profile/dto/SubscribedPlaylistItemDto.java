package org.example.mopl.profile.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscribedPlaylistItemDto {

    private final UUID playlistUuid;
    private final String title;
    private final String description;
    private final UUID ownerUuid;
    private final String ownerName;
    private final String ownerProfileImageUrl;
    private final Long subscriberCount;
    private final Instant updatedAt;
}
