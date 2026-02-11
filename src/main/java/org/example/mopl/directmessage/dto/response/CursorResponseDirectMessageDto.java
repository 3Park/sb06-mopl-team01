package org.example.mopl.directmessage.dto.response;

import lombok.Builder;
import org.example.mopl.directmessage.dto.data.DirectMessageDto;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

@Builder
public record CursorResponseDirectMessageDto(
        List<DirectMessageDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {}
