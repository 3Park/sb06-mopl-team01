package org.example.mopl.notification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;

public record NotificationListRequest(
        String cursor,
        UUID idAfter,
        @NotNull @Min(1) int limit,
        @NotNull SortDirection sortDirection,
        @NotNull SortBy sortBy
        ) {
}
