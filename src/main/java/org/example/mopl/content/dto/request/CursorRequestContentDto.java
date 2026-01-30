package org.example.mopl.content.dto.request;

import java.util.List;
import java.util.UUID;

public record CursorRequestContentDto(
    String typeEqual,
    String keywordLike,
    List<String> tagsIn,
    String cursor,
    UUID idAfter,
    Integer limit,
    String sortDirection,
    String sortBy
) {

}
