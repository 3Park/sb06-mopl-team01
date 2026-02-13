package org.example.mopl.directmessage.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.directmessage.entity.QConversation;
import org.example.mopl.directmessage.entity.QDirectMessage;
import org.example.mopl.directmessage.enums.SortBy;
import org.example.mopl.directmessage.dto.ConversationSearchCondition;
import org.example.mopl.directmessage.entity.Conversation;

import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.entity.QUser;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QConversation conversation = QConversation.conversation;
    private final QDirectMessage message = QDirectMessage.directMessage;
    private final QUser user = QUser.user;
    private final QProfile profile = QProfile.profile;


    @Override
    public Optional<Conversation> findExisting(Long userId1, Long userId2) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(conversation)
                        .where(hasParticipants(userId1, userId2))
                        .fetchFirst()
        );
    }

    @Override
    public boolean existsByConversationUuidAndUserUuid(UUID conversationUuid, UUID userUuid) {
        return jpaQueryFactory
                .selectOne()
                .from(conversation)
                .where(
                        conversation.uuid.eq(conversationUuid),
                        isUserUuidEq(userUuid)
                )
                .fetchFirst() != null;
    }

    @Override
    public List<Conversation> searchByCursor(ConversationSearchCondition condition) {
        return jpaQueryFactory
                .selectFrom(conversation)
                .where(
                        isParticipant(condition),
                        isAfterCursor(condition),
                        containsKeyword(condition)
                )
                .limit(condition.limit())
                .orderBy(orderBy(condition))
                .fetch();
    }

    @Override
    public Long countByCursor(ConversationSearchCondition condition) {
        return jpaQueryFactory
                .select(conversation.count())
                .from(conversation)
                .where(
                        isParticipant(condition),
                        containsKeyword(condition)
                )
                .fetchOne();
    }

    private BooleanExpression hasParticipants(Long userId1, Long userId2) {
        return isCreatorAndJoiner(userId1, userId2).or(isCreatorAndJoiner(userId2, userId1));
    }
    private BooleanExpression isCreatorAndJoiner(Long userId1, Long userId2) {
        return conversation.creatorId.eq(userId1).and(conversation.joinId.eq(userId2));
    }


    // === Where ===

    private BooleanExpression isUserUuidEq (UUID userUuid) {
        return JPAExpressions
                .selectOne()
                .from(user)
                .where(
                        user.uuid.eq(userUuid),
                        conversation.creatorId.eq(user.id)
                                .or(conversation.joinId.eq(user.id))
                )
                .exists();
    }

    private BooleanExpression isParticipant(ConversationSearchCondition condition) {
        return conversation.creatorId.eq(condition.requesterId())
                .or(conversation.joinId.eq(condition.requesterId()));
    }
    private BooleanExpression isAfterCursor(ConversationSearchCondition condition) {
        if (condition.idAfter() == null) {return null;}
        UUID idAfter = condition.idAfter();

        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch (sortBy) {
            case createdAt:
                return isAsc?
                        conversation.id.gt(getConversationIdByUuid(idAfter))
                        : conversation.id.lt(getConversationIdByUuid(idAfter));
            default:
                throw new IllegalArgumentException("지원하지 않는 sortBy 타입: " + sortBy);
        }
    }
    private BooleanExpression containsKeyword(ConversationSearchCondition condition) {
        if (condition.keywordLike() == null || condition.keywordLike().isBlank()) {return null;}

        String keyword = condition.keywordLike();

        BooleanExpression nameMatch = JPAExpressions
                .selectOne().from(profile).where(
                        profile.user.id.eq(conversation.joinId)
                        .or(profile.user.id.eq(conversation.creatorId)),
                        profile.name.contains(keyword)
                        ).exists();

        BooleanExpression messageContentMatch = JPAExpressions
                .selectOne().from(message).where(message.conversation.id.eq(conversation.id),
                        message.content.contains(keyword)).exists();

        return nameMatch.or(messageContentMatch);
    }

    // === OrderBy ===
    private OrderSpecifier<?> orderBy(ConversationSearchCondition condition) {
        boolean isAsc = (condition.sortDirection() == SortDirection.ASCENDING);
        SortBy sortBy = condition.sortBy();

        switch(sortBy) {
            case createdAt :
                return isAsc? conversation.id.asc() : conversation.id.desc();
            default :
                throw new IllegalArgumentException("지원하지 않는 sortBy 타입: " + sortBy);
        }
    }

    // === SubQuery Helper ===
    private JPQLQuery<Long> getConversationIdByUuid(UUID conversationUuid) {
        return JPAExpressions
                .select(conversation.id)
                .from(conversation)
                .where(conversation.uuid.eq(conversationUuid));
    }
}
