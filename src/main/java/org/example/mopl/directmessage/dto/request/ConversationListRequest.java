package org.example.mopl.directmessage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

public record ConversationListRequest(
        String keywordLike,
        String cursor,
        UUID idAfter,
        @Min(1) int limit,
        @NotNull SortDirection sortDirection,
        @NotNull SortBy sortBy
        ) {
}
