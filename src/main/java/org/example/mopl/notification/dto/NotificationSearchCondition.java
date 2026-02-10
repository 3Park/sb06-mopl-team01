package org.example.mopl.notification.dto;

import org.example.mopl.notification.dto.request.NotificationListRequest;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;


public record NotificationSearchCondition(
        UUID receiverId,
        Long idAfter,
        int limit,
        SortDirection sortDirection,
        SortBy sortBy
) {
}
