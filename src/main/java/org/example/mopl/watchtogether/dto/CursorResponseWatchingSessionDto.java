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
    public static CursorResponseWatchingSessionDto toDto(
            List<WatchingSessionDto> data,
            Integer limit,
            long totalCount,
            String sortBy,
            String sortDirection
    ){
        boolean hasNext = data.size() == limit+1;

        String newCursor = null;
        String newIdAfter = null;

        if(hasNext){
            WatchingSessionDto lastData = data.get(data.size()-1);
            newCursor = lastData.watcher().getName();
            newIdAfter = lastData.watcher().getUserId().toString();
            data = data.subList(0,data.size()-1);
        }

        return new CursorResponseWatchingSessionDto(
                data,
                newCursor,
                newIdAfter,
                hasNext,
                Math.toIntExact(totalCount),
                sortBy,
                sortDirection
        );
    }
}
