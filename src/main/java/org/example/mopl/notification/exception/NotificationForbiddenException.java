package org.example.mopl.notification.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class NotificationForbiddenException extends MoplException {
    public NotificationForbiddenException(UUID userId) {
        super(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        addDetail("user", userId);
    }
}
