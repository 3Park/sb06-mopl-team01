package org.example.mopl.directmessage.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class ConversationNotFoundException extends MoplException {

    public ConversationNotFoundException(UUID conversationUuid) {
        super(DirectMessageErrorCode.CONVERSATION_NOT_FOUND);
        addDetail("conversationId", conversationUuid);
    }
}
