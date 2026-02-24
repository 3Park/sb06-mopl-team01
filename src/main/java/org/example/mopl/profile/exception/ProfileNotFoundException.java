package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class ProfileNotFoundException extends MoplException {

    public ProfileNotFoundException(UUID userUuid) {
        super(ProfileErrorCode.PROFILE_NOT_FOUND);
        addDetail("userUuid", userUuid);
    }
}
