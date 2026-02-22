package org.example.mopl.directmessage.dto;

public record TypingRequest(
        boolean isTyping
) {
    public static TypingRequest of(boolean isTyping) {
        return new TypingRequest(isTyping);
    }
}
