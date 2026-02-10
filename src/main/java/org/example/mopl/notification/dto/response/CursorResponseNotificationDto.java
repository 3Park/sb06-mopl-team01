package org.example.mopl.notification.dto.response;

import lombok.Builder;
import org.example.mopl.notification.dto.data.NotificationDto;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

@Builder
public record CursorResponseNotificationDto(
        List<NotificationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {
}
