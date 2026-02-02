package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CursorRequestContentDto(
    String typeEqual,
    String keywordLike,
    List<String> tagsIn,
    String cursor,
    UUID idAfter,
    @NotNull
    Integer limit,
    @NotBlank
    String sortDirection,
    @NotBlank
    String sortBy
) {

}
