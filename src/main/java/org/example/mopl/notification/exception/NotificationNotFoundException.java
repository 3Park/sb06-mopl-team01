package org.example.mopl.notification.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class NotificationNotFoundException extends MoplException {

    public NotificationNotFoundException(UUID notificationId) {
        super(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        addDetail("notificationId", notificationId);
    }
}
