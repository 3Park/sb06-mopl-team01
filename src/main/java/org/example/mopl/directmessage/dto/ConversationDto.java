package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.exception.DirectMessageForbiddenException;
import org.example.mopl.user.entity.User;

import java.util.UUID;

public record ConversationDto(
        UUID id,
        SimpleUserDto with,
        DirectMessageDto lastestMessage,
        boolean hasUnread
) {
    public static ConversationDto from(
            Conversation conversation, User requester, User other, DirectMessage message
    ) {
        if (message == null) {
            return new ConversationDto(conversation.getUuid(), SimpleUserDto.from(other),
                    null, false);
        }

        if (!message.isValidParticipants(requester.getId(), other.getId())) {
            throw new DirectMessageForbiddenException();
        }

        User sender = message.isSenderId(requester.getId())? requester : other;
        User receiver = message.isSenderId(requester.getId())? other : requester;

        boolean hasUnread = message.isUnreadBy(requester.getId());

        return new ConversationDto(
                conversation.getUuid(),
                SimpleUserDto.from(other),
                DirectMessageDto.from(message, sender, receiver),
                hasUnread
        );
    }
}
