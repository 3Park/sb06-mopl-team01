package org.example.mopl.directmessage.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class DirectMessageNotFoundException extends MoplException {
    public DirectMessageNotFoundException(UUID directMessageUuid) {
        super(DirectMessageErrorCode.DIRECT_MESSAGE_NOT_FOUND);
        addDetail("directMessageId", directMessageUuid);
    }
}
