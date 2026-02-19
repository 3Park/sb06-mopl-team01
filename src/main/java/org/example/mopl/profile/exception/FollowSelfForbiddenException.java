package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class FollowSelfForbiddenException extends MoplException {

    public FollowSelfForbiddenException() {
        super(ProfileErrorCode.FOLLOW_SELF_FORBIDDEN);
    }
}
