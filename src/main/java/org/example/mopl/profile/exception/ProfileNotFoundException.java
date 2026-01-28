package org.example.mopl.profile.exception;

import org.example.mopl.common.exception.MoplException;

public class ProfileNotFoundException extends MoplException {

    public ProfileNotFoundException(Long userId) {
        super("프로필을 찾을 수 없습니다. userId=" + userId);
    }
}
