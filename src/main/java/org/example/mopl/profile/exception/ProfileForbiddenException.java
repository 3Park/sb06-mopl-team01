package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class ProfileForbiddenException extends MoplException {

    public ProfileForbiddenException() {
        super(ProfileErrorCode.PROFILE_FORBIDDEN);
    }
}
