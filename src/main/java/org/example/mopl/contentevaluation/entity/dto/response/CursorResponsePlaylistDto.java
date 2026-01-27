package org.example.mopl.contentevaluation.entity.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record CursorResponsePlaylistDto(
    List<PlaylistDto> data,
    String nextCursor,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
