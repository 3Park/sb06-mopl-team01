package org.example.mopl.directmessage.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.directmessage.entity.Conversation;

import org.example.mopl.conversation.entity.QConversation;
import org.example.mopl.user.entity.QUser;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QConversation conversation = QConversation.conversation;
    private final QUser user = QUser.user;


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
                .join(user).on(isCreatorOrJoiner())
                .where(
                        conversation.uuid.eq(conversationUuid),
                        user.uuid.eq(userUuid)
                )
                .fetchFirst() != null;
    }

    private BooleanExpression hasParticipants(Long userId1, Long userId2) {
        return isCreatorAndJoiner(userId1, userId2).or(isCreatorAndJoiner(userId2, userId1));
    }
    private BooleanExpression isCreatorAndJoiner(Long userId1, Long userId2) {
        return conversation.creatorId.eq(userId1).and(conversation.joinId.eq(userId2));
    }
    private BooleanExpression isCreatorOrJoiner() {
        return user.id.eq(conversation.creatorId).or(user.id.eq(conversation.joinId));
    }
}
