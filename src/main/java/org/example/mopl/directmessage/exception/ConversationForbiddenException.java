package org.example.mopl.directmessage.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class ConversationForbiddenException extends MoplException {
    public ConversationForbiddenException(UUID conversationId) {
        super(DirectMessageErrorCode.CONVERSATION_FORBIDDEN);
        addDetail("conversationId", conversationId);
    }
}
