package org.example.mopl.notification.dto;

import org.example.mopl.notification.dto.request.NotificationListRequest;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;


public record NotificationSearchCondition(
        UUID receiverId,

        String cursor,
        Long idAfter,
        int limit,
        SortDirection sortDirection,
        SortBy sortBy
) {
    public static NotificationSearchCondition of(NotificationListRequest request, UUID receiverId, Long idAfter) {
        return new NotificationSearchCondition(
                receiverId, request.cursor(), idAfter, request.limit(), request.sortDirection(), request.sortBy()
        );
    }
}
