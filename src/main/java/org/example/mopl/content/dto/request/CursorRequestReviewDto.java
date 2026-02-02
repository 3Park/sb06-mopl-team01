package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CursorRequestReviewDto(
    UUID contentId,
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
