package org.example.mopl.content.entity.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record CursorRequestContentDto(
    String typeEqual,
    String keywordLike,
    List<String> tagsIn,
    String cursor,
    String idAfter,
    Integer limit,
    String sortDirection,
    String sortBy
) {

}
