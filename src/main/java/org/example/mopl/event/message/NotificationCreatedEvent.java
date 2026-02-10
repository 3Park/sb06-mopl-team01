package org.example.mopl.event.message;

import org.example.mopl.notification.dto.data.NotificationDto;

public record NotificationCreatedEvent(
        NotificationDto notificationDto
) {
    public static NotificationCreatedEvent of(NotificationDto notificationDto) {
        return new NotificationCreatedEvent(notificationDto);
    }
}
