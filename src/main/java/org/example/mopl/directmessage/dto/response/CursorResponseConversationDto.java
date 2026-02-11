package org.example.mopl.directmessage.dto.response;

import lombok.Builder;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

@Builder
public record CursorResponseConversationDto(
        List<ConversationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {}
