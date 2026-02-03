package org.example.mopl.user.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

public class CursorResponseUserDto {
    List<UserDto> data;
    String nextCursor;
    UUID nextIdAfter;
    boolean hasNext;
    Long totalCount;
    String sortBy;
    String sortDirection;

    @Builder
    public CursorResponseUserDto(List<UserDto> data,
                                 String nextCursor,
                                 UUID nextIdAfter,
                                 boolean hasNext,
                                 Long totalCount,
                                 String sortBy,
                                 String sortDirection) {
        this.data = data;
        this.nextCursor = nextCursor;
        this.nextIdAfter = nextIdAfter;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
}
