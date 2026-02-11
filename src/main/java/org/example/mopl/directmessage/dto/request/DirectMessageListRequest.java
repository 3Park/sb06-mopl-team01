package org.example.mopl.directmessage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.mopl.directmessage.dto.DirectMessageSearchCondition;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

public record DirectMessageListRequest(
        String cursor,
        UUID idAfter,
        @Min(1) int limit,
        @NotNull SortDirection sortDirection,
        @NotNull SortBy sortBy
        ) {
    public DirectMessageSearchCondition toSearchCondition(
            Long requesterId, Long conversationId, int limit) {
        return DirectMessageSearchCondition.builder()
                .idAfter(idAfter)
                .limit(limit)
                .sortDirection(sortDirection)
                .sortBy(sortBy)
                .requesterId(requesterId)
                .conversationId(conversationId)
                .build();
    }
}
