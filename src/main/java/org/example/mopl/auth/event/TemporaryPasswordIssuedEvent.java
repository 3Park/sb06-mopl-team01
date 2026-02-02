package org.example.mopl.auth.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TemporaryPasswordIssuedEvent {
    private String temporaryPassword;
    private String receiverEmail;

    @Builder
    public TemporaryPasswordIssuedEvent(String temporaryPassword, String receiverEmail) {
        this.temporaryPassword = temporaryPassword;
        this.receiverEmail = receiverEmail;
    }
}
