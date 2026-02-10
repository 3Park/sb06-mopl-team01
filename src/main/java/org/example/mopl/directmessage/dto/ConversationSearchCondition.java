package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

public record ConversationSearchCondition(
        String keywordLike,
        UUID idAfter,
        int limit,
        SortDirection sortDirection,
        SortBy sortBy,
        Long requesterId
) {
    public static ConversationSearchCondition of(
            String keywordLike, UUID idAfter, int limit,
            SortDirection sortDirection, SortBy sortBy, Long requesterId
    ) {
        return new ConversationSearchCondition(
                keywordLike, idAfter, limit, sortDirection, sortBy, requesterId
        );
    }
}
