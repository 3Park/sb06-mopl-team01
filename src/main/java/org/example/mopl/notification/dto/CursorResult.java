package org.example.mopl.notification.dto;

import org.example.mopl.notification.entity.Notification;

import java.util.List;
import java.util.UUID;

public record CursorResult(
        List<Notification> notifications,
        boolean hasNext,
        String nextCursor,
        UUID nextIdAfter
) {
}
