package org.example.mopl.directmessage.dto.response;

import org.example.mopl.directmessage.dto.ConversationSearchCondition;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.enums.SortBy;
import org.example.mopl.user.entity.User;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CursorResponseConversationDto(
        List<ConversationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {
    public static CursorResponseConversationDto of(
            List<Conversation> conversations, Long totalCount, ConversationSearchCondition condition,
            Map<Long, User> otherMap, Map<Long, DirectMessage> lastMessageMap, User requester) {

        int limit = condition.limit() - 1;
        UUID nextIdAfter = null;
        String nextCursor = null;
        boolean hasNext = false;

        if (conversations.size() > limit) {
            hasNext = true;
            conversations = conversations.subList(0, limit);
        }

        if (!conversations.isEmpty()) {
            nextIdAfter = conversations.get(conversations.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }

        List<ConversationDto> data = conversations.stream()
                .map(conversation -> {
                    Long otherId = conversation.getCounterpartId(requester.getId());
                    User other = otherMap.get(otherId);
                    DirectMessage lastMessage = lastMessageMap.get(conversation.getId());
                    return ConversationDto.from(conversation, requester, other, lastMessage);
                })
                .toList();

        return new CursorResponseConversationDto(
                data, nextCursor, nextIdAfter, hasNext,
                totalCount, condition.sortBy(), condition.sortDirection()
        );
    }
}
