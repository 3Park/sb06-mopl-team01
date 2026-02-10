package org.example.mopl.directmessage.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.directmessage.dto.DirectMessageSearchCondition;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.entity.QDirectMessage;
import org.example.mopl.directmessage.enums.SortBy;
import org.hibernate.query.SortDirection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QDirectMessage directMessage = QDirectMessage.directMessage;

    @Override
    public Long markAsRead(Long conversationId, Long requesterId) {
        return jpaQueryFactory
                .update(directMessage)
                .set(directMessage.isRead, true)
                .where(isUnreadIn(conversationId),
                        directMessage.receiverId.eq(requesterId))
                .execute();
    }

    @Override
    public List<DirectMessage> searchByCursor(DirectMessageSearchCondition condition) {
        return jpaQueryFactory
                .selectFrom(directMessage)
                .where(
                        directMessage.conversation.id.eq(condition.conversationId()),
                        isAfterCursor(condition)
                )
                .limit(condition.limit())
                .orderBy(orderBy(condition))
                .fetch();
    }

    @Override
    public Long countByConversationId(Long conversationId) {
        return jpaQueryFactory
                .select(directMessage.count())
                .from(directMessage)
                .where(
                        directMessage.conversation.id.eq(conversationId)
                )
                .fetchOne();
    }

    @Override
    public Map<Long, DirectMessage> findAllLastMessagesByConversationIdIn(List<Long> ids) {
        if (ids.isEmpty()) {return Collections.emptyMap();}

        List<Long> lastMessageIds = jpaQueryFactory
                .select(directMessage.id.max())
                .from(directMessage)
                .where(directMessage.conversation.id.in(ids))
                .groupBy(directMessage.conversation.id)
                .fetch();

        List<DirectMessage> lastMessages = jpaQueryFactory
                .selectFrom(directMessage)
                .where(directMessage.id.in(lastMessageIds))
                .fetch();

        return lastMessages.stream()
                .collect(Collectors.toMap(dm -> dm.getConversation().getId(), dm -> dm));
    }


    private BooleanExpression isUnreadIn(Long conversationId) {
        return directMessage.isRead.eq(false)
                .and(directMessage.conversation.id.eq(conversationId));
    }

    private BooleanExpression isAfterCursor(DirectMessageSearchCondition condition) {
        if (condition.idAfter() == null) {return null;}
        UUID idAfter = condition.idAfter();

        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch (sortBy) {
            case createdAt :
                return isAsc?
                        directMessage.id.gt(getDirectMessageIdByUuid(idAfter))
                        : directMessage.id.lt(getDirectMessageIdByUuid(idAfter));
            default:
                throw new IllegalArgumentException("지원하지 않는 sortBy 타입: " + sortBy);
        }
    }

    private OrderSpecifier<?> orderBy(DirectMessageSearchCondition condition) {
        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch(sortBy) {
            case createdAt :
                return isAsc?
                        directMessage.id.asc() : directMessage.id.desc();
            default :
                throw new IllegalArgumentException("지원하지 않는 sortBy 타입: " + sortBy);
        }
    }
    private JPQLQuery<Long> getDirectMessageIdByUuid(UUID directMessageUuid) {
        return JPAExpressions
                .select(directMessage.id)
                .from(directMessage)
                .where(directMessage.uuid.eq(directMessageUuid));
    }
}
