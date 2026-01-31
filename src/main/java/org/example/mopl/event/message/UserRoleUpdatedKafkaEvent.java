package org.example.mopl.event.message;

import java.util.UUID;

public record UserRoleUpdatedKafkaEvent(
        UUID receiverId,

        // TODO: Role -> Enum 사용 (교체하기)
        String beforeRole,
        String afterRole
) implements KafkaEvent {

    public static final String TOPIC = "mopl.user.role.updated";

    @Override
    public String topic() { return TOPIC;}

    public static UserRoleUpdatedKafkaEvent of (UUID receiverId, String beforeRole, String afterRole) {
        return new UserRoleUpdatedKafkaEvent(receiverId, beforeRole, afterRole);
    }
}
