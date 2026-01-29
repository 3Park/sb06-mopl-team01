package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class ProfileNotFoundException extends MoplException {

    public ProfileNotFoundException(Long userId) {
        super(ProfileErrorCode.PROFILE_NOT_FOUND);
        addDetail("userId", userId);
    }
}
