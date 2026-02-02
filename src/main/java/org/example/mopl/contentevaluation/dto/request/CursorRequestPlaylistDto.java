package org.example.mopl.contentevaluation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CursorRequestPlaylistDto(
    String keywordLike,
    UUID ownerIdEqual,
    UUID subscriberIdEqual,
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
