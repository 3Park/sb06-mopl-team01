package org.example.mopl.directmessage.exception;

import org.example.mopl.common.exception.MoplException;

import java.util.UUID;

public class ParticipantNotFoundException extends MoplException {
    public ParticipantNotFoundException(UUID userId) {
        super(DirectMessageErrorCode.PARTICIPANT_NOT_FOUND);
        addDetail("participantId", userId);
    }
    public ParticipantNotFoundException() {
        super(DirectMessageErrorCode.PARTICIPANT_NOT_FOUND);
    }
}
