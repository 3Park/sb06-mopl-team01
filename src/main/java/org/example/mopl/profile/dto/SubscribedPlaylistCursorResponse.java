package org.example.mopl.profile.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscribedPlaylistCursorResponse {

    private final List<SubscribedPlaylistItemDto> data;
    private final String nextCursor;
    private final UUID nextIdAfter;
    private final Boolean hasNext;
    private final String sortBy;
    private final String sortDirection;
}
