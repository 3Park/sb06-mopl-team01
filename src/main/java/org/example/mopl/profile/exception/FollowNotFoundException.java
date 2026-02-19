package org.example.mopl.profile.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class FollowNotFoundException extends MoplException {

    public FollowNotFoundException(UUID followUuid) {
        super(ProfileErrorCode.FOLLOW_NOT_FOUND);
        addDetail("followUuid", followUuid);
    }
}
