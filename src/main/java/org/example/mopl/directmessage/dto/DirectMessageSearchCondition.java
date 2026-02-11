package org.example.mopl.directmessage.dto;

import lombok.Builder;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

@Builder
public record DirectMessageSearchCondition(
        UUID idAfter,
        int limit,
        SortDirection sortDirection,
        SortBy sortBy,
        Long requesterId,
        Long conversationId
) {
}
