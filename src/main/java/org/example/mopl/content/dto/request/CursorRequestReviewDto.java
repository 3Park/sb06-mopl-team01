package org.example.mopl.content.dto.request;

import java.util.UUID;

public record CursorRequestReviewDto(
    UUID contentId,
    String cursor,
    String idAfter,
    Integer limit,
    String sortDirection,
    String sortBy
) {

}
