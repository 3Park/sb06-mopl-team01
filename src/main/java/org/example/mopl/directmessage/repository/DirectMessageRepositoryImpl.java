package org.example.mopl.directmessage.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.conversation.entity.QConversation;
import org.example.mopl.directmessage.entity.QDirectMessage;

@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QDirectMessage directMessage = QDirectMessage.directMessage;
    private final QConversation conversation = QConversation.conversation;

    @Override
    public Long readUnreadMessages(Long conversationId, Long requesterId) {
        return jpaQueryFactory
                .update(directMessage)
                .set(directMessage.readStatus, true)
                .where(isUnreadIn(conversationId),
                        directMessage.receiverId.eq(requesterId))
                .execute();
    }

    private BooleanExpression isUnreadIn(Long conversationId) {
        return directMessage.readStatus.eq(false)
                .and(directMessage.conversation.id.eq(conversationId));
    }
}
