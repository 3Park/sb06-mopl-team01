package org.example.mopl.user.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CursorResponseUserDto(
        List<UserDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        String sortBy,
        String sortDirection
) {

}
