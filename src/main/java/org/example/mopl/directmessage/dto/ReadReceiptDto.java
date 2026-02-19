package org.example.mopl.directmessage.dto;

import org.example.mopl.directmessage.enums.Type;

import java.util.UUID;

public record ReadReceiptDto(
        Type type,
        UUID conversationId,
        UUID lastReadMessageId,
        UUID readerId
) {
    public static ReadReceiptDto of(Type type, UUID conversationId,
                                    UUID lastReadMessageId, UUID readerId) {
        return new ReadReceiptDto(type, conversationId, lastReadMessageId, readerId);
    }
}
