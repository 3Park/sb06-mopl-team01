package org.example.mopl.event.message;

import java.util.UUID;

public record UserFollowCreatedKafkaEvent(
        UUID receiverId,

        String followerName
) implements KafkaEvent {

    public static final String TOPIC = "mopl.user.follow.created";

    @Override
    public String topic() { return TOPIC;}

    public static UserFollowCreatedKafkaEvent of(UUID receiverId, String followerName) {
        return new UserFollowCreatedKafkaEvent(receiverId, followerName);
    }
}
