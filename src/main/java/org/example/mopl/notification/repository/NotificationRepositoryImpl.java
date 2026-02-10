package org.example.mopl.notification.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.notification.dto.NotificationSearchCondition;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.entity.QNotification;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    private final QNotification notification = QNotification.notification;

    @Override
    public List<Notification> searchByCursor(NotificationSearchCondition condition) {
        return jpaQueryFactory
                .selectFrom(notification)
                .where(
                        condition.where(notification)
                )
                .orderBy(condition.orderBy(notification))
                .limit(condition.limit())
                .fetch();
    }
}

