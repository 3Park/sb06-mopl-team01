package org.example.mopl.content.entity.dto.response;

import java.util.List;

public record CursorResponseReviewDto(
    List<ReviewDto> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
