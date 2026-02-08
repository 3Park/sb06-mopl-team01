package org.example.mopl.directmessage.dto.data;

import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
        UUID id,
        UUID conversationId,
        LocalDateTime createdAt,
        SimpleUserDto sender,
        SimpleUserDto receiver,
        String content
) {
    public static DirectMessageDto from(DirectMessage directMessage, User sender, User receiver) {
        return new DirectMessageDto(directMessage.getUuid(),
                directMessage.getConversation().getUuid(),
                directMessage.getCreatedAt(),
                SimpleUserDto.from(sender),
                SimpleUserDto.from(receiver),
                directMessage.getContent()
        );
    }
}
