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
    @NotNull(message = "limit는 null일 수 없습니다.")
    Integer limit,
    @NotBlank(message = "sortDirection은 blank일 수 없습니다.")
    String sortDirection,
    @NotBlank(message = "sortBy는 blank일 수 없습니다.")
    String sortBy
) {

}
