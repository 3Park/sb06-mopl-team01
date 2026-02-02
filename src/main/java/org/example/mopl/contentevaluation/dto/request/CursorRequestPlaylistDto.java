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
    @NotNull
    Integer limit,
    @NotBlank
    String sortDirection,
    @NotBlank
    String sortBy
) {

}
