package org.example.mopl.notification.dto.data;

import lombok.Builder;
import org.example.mopl.notification.enums.Level;
import org.example.mopl.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationDto(
        UUID id,
        LocalDateTime createdAt,
        UUID receiverId,
        String title,
        String content,
        Level level
) {
    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getUuid())
                .createdAt(notification.getCreatedAt())
                .receiverId(notification.getReceiverId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .level(notification.getLevel())
                .build();
    }
}
