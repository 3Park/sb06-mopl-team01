package org.example.mopl.watchtogether.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CursorResponseWatchingSessionDto (
        List<WatchingSessionDto> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        Integer totalCount,
        String sortBy,
        String sortDirection
){
}
