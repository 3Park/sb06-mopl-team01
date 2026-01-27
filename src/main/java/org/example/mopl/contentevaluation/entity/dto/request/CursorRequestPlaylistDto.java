package org.example.mopl.contentevaluation.entity.dto.request;

import java.util.UUID;

public record CursorRequestPlaylistDto(
    String keywordLike,
    UUID ownerIdEqual,
    UUID subscriberIdEqual,
    String cursor,
    UUID idAfter,
    Integer limit,
    String sortDirection,
    String sortBy
) {

}
