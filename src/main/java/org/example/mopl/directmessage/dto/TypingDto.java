package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.enums.Type;

import java.util.UUID;

public record TypingDto(
        Type type,
        UUID conversationId,
        UUID senderId,
        boolean isTyping
) {
    public static TypingDto of(UUID conversationId, UUID senderId, boolean isTyping) {
        return new TypingDto(Type.TYPING, conversationId, senderId, isTyping);
    }
}
