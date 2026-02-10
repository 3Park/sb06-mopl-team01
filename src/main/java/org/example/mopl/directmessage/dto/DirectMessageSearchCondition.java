package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

public record DirectMessageSearchCondition(
        UUID idAfter,
        int limit,
        SortDirection sortDirection,
        SortBy sortBy,
        Long requesterId,
        Long conversationId
) {
    public static DirectMessageSearchCondition of(
            UUID idAfter, int limit, SortDirection sortDirection,
            SortBy sortBy, Long requesterId, Long conversationId
    ) {
        return new DirectMessageSearchCondition(idAfter, limit, sortDirection, sortBy, requesterId, conversationId);
    }
}
