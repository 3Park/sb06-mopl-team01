package org.example.mopl.user.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.example.mopl.common.exception.MoplException;

public class UserException extends MoplException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
