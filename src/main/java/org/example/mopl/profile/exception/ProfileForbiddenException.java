package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class ProfileForbiddenException extends MoplException {

    public ProfileForbiddenException() {
        super("본인의 프로필만 수정할 수 있습니다.");
    }
}
