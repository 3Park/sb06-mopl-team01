package org.example.mopl.directmessage.exception;

import org.example.mopl.common.exception.MoplException;

public class DirectMessageForbiddenException extends MoplException {
    public DirectMessageForbiddenException() {
        super(DirectMessageErrorCode.DIRECT_MESSAGE_FORBIDDEN);
    }
}
