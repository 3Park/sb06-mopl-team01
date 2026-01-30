package org.example.mopl.auth.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.example.mopl.common.exception.MoplException;

public class AuthException extends MoplException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
