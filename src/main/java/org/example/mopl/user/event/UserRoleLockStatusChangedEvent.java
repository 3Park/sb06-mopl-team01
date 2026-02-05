package org.example.mopl.user.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserRoleLockStatusChangedEvent {
    private String userEmail;

    @Builder
    public  UserRoleLockStatusChangedEvent(String userEmail) {
        this.userEmail = userEmail;
    }
}
