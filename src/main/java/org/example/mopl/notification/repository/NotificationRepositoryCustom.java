package org.example.mopl.notification.repository;

import org.example.mopl.notification.dto.NotificationSearchCondition;
import org.example.mopl.notification.entity.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<Notification> searchByCursor(NotificationSearchCondition condition);
}
