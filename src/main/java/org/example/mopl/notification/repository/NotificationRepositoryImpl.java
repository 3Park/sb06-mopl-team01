package org.example.mopl.notification.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.notification.dto.NotificationSearchCondition;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.entity.QNotification;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QNotification notification = QNotification.notification;

    @Override
    public List<Notification> searchByCursor(NotificationSearchCondition condition) {
        return jpaQueryFactory
                .selectFrom(notification)
                .where(
                        notification.receiverId.eq(condition.receiverId()),
                        isIdAfter(condition)
                )
                .orderBy(orderBy(condition))
                .limit(condition.limit())
                .fetch();
    }

    private OrderSpecifier<?> orderBy(NotificationSearchCondition condition) {
        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch (sortBy) {
            case createdAt:
                return isAsc ?
                        notification.id.asc()
                        : notification.id.desc();
            default:
                throw new IllegalArgumentException("지원하지 않는 sortBy : " + sortBy);
        }
    }

    private BooleanExpression isIdAfter(NotificationSearchCondition condition) {
        if (condition.idAfter() == null) return null;
        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch (sortBy) {
            case createdAt:
                return isAsc ?
                        // id는 시간순으로 생성되고 있으므로 id 기준 정렬 사용
                        notification.id.gt(condition.idAfter())
                        : notification.id.lt(condition.idAfter());
            default:
                throw new IllegalArgumentException("지원하지 않는 sortBy : " + sortBy);
        }
    }
}

