package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
        UUID id,
        UUID conversationId,
        LocalDateTime createdAt,
        UserDto sender,
        UserDto receiver,
        String content
) {

    private record UserDto(
            UUID userId,
            String name,
            String profileImageUrl
    ){
        private static UserDto from(User user) {
            return new UserDto(
                    user.getUuid(),
                    user.getProfile().getName(),
                    user.getProfile().getProfileImageUrl()
            );
        }
    }

    public static DirectMessageDto from(DirectMessage directMessage, User sender, User receiver) {
        return new DirectMessageDto(directMessage.getUuid(),
                directMessage.getConversation().getUuid(),
                directMessage.getCreatedAt(),
                UserDto.from(sender),
                UserDto.from(receiver),
                directMessage.getContent()
        );
    }
}
