package org.example.mopl.notification.dto;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.example.mopl.notification.entity.QNotification;
import org.example.mopl.notification.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.UUID;


@Builder
public record NotificationSearchCondition(
        UUID receiverId,

        String cursor,
        Long idAfter,
        @NotNull @Min(1) int limit,
        @NotNull SortDirection sortDirection,
        @NotNull SortBy sortBy
) {

    public OrderSpecifier<?> orderBy(QNotification notification) {
        return (sortDirection == SortDirection.ASCENDING) ?
                notification.id.asc() : notification.id.desc();
    }

    public BooleanExpression where(QNotification notification) {
        return notification.receiverId.eq(receiverId)
                .and(idAfterCondition(notification));
    }

    private BooleanExpression idAfterCondition(QNotification notification) {
        if (idAfter == null) return null;
        return (sortDirection == SortDirection.ASCENDING) ?
                notification.id.gt(idAfter) : notification.id.lt(idAfter);
    }
}
