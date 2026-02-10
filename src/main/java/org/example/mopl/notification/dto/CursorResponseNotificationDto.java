package org.example.mopl.notification.dto;

import lombok.Builder;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto(
        List<NotificationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {
    public static CursorResponseNotificationDto of(
            List<Notification> notifications, int conditionLimit, Long totalCount, SortBy sortBy, SortDirection sortDirection
    ) {
        boolean hasNext = false;
        int limit = conditionLimit - 1;

        UUID nextIdAfter = null;
        String nextCursor = null;

        if (notifications.size() > limit) {
            hasNext = true;
            notifications = notifications.subList(0, limit);
        }

        if (!notifications.isEmpty()) {
            nextIdAfter = notifications.get(notifications.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }

        List<NotificationDto> data = notifications.stream()
                .map(NotificationDto::from).toList();

        return new CursorResponseNotificationDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                sortBy,
                sortDirection
        );
    }
}
