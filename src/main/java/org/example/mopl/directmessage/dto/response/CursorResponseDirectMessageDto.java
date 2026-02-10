package org.example.mopl.directmessage.dto.response;

import lombok.Builder;
import org.example.mopl.directmessage.dto.DirectMessageSearchCondition;
import org.example.mopl.directmessage.dto.data.DirectMessageDto;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.enums.SortBy;
import org.example.mopl.user.entity.User;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

@Builder
public record CursorResponseDirectMessageDto(
        List<DirectMessageDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {
    public static CursorResponseDirectMessageDto of(
            List<DirectMessage> directMessages, Long totalCount, DirectMessageSearchCondition condition,
            User requester, User other
    ) {
        boolean hasNext = false;
        int limit = condition.limit() - 1;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (directMessages.size() > limit) {
            hasNext = true;
            directMessages = directMessages.subList(0, limit);
        }

        if (!directMessages.isEmpty()) {
            nextIdAfter = directMessages.get(directMessages.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }

        List<DirectMessageDto> data = directMessages.stream()
                .map(directMessage -> {
                    boolean isMe = directMessage.isSenderId(requester.getId());
                    User sender = isMe ? requester : other;
                    User receiver = isMe ? other : requester;
                    return DirectMessageDto.from(directMessage, sender, receiver);
                })
                .toList();

        return CursorResponseDirectMessageDto.builder()
                .data(data)
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(condition.sortBy())
                .sortDirection(condition.sortDirection())
                .build();
    }
}
