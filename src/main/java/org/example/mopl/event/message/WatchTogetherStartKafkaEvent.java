package org.example.mopl.event.message;

import java.util.List;
import java.util.UUID;

public record WatchTogetherStartKafkaEvent(
        List<UUID> receiverIds,

        String watcherName,
        String contentName
) implements KafkaEvent {
    public static final String TOPIC = "mopl.watchTogether.start";

    @Override
    public String topic() { return TOPIC;}

    public static WatchTogetherStartKafkaEvent of (List<UUID> followerIds, String watcherName, String contentName) {
        return new  WatchTogetherStartKafkaEvent(followerIds, watcherName, contentName);
    }
}
