package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class FollowAlreadyExistsException extends MoplException {

    public FollowAlreadyExistsException(Long followerId, Long followeeId) {
        super(ProfileErrorCode.FOLLOW_ALREADY_EXISTS);
        addDetail("followerId", followerId);
        addDetail("followeeId", followeeId);
    }

    public FollowAlreadyExistsException(UUID followerUuid, UUID followeeUuid) {
        super(ProfileErrorCode.FOLLOW_ALREADY_EXISTS);
        addDetail("followerUuid", followerUuid);
        addDetail("followeeUuid", followeeUuid);
    }
}
