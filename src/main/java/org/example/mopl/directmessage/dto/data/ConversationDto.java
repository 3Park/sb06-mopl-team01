package org.example.mopl.directmessage.dto.data;

import lombok.extern.slf4j.Slf4j;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.user.entity.User;

import java.util.UUID;

@Slf4j
public record ConversationDto(
        UUID id,
        SimpleUserDto with,
        DirectMessageDto lastestMessage,
        boolean hasUnread
) {
    public static ConversationDto from(
            Conversation conversation, User requester, User other, DirectMessage lastMessage
    ) {
        if (lastMessage == null) {
            return new ConversationDto(conversation.getUuid(), SimpleUserDto.from(other),
                    null, false);
        }

        if (!lastMessage.isValidParticipants(requester.getId(), other.getId())) {
            log.error("lastMessageId={} 잘못된 참여자입니다.", lastMessage.getUuid());
            return null;
        }

        User sender = lastMessage.isSenderId(requester.getId())? requester : other;
        User receiver = lastMessage.isSenderId(requester.getId())? other : requester;

        boolean hasUnread = lastMessage.isUnreadBy(requester.getId());

        return new ConversationDto(
                conversation.getUuid(),
                SimpleUserDto.from(other),
                DirectMessageDto.from(lastMessage, sender, receiver),
                hasUnread
        );
    }
}
