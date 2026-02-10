package org.example.mopl.directmessage.dto.condition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
