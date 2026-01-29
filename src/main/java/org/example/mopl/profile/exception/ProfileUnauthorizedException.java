package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class ProfileUnauthorizedException extends MoplException {

    public ProfileUnauthorizedException() {
        super(ProfileErrorCode.PROFILE_UNAUTHORIZED);
    }
}
