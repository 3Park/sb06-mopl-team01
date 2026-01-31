package org.example.mopl.event.message;

import java.util.UUID;

public record PlaylistSubscriptionCreatedKafkaEvent(
        UUID receiverId,

        String subscriberName,
        String playlistTitle,
        String playlistDescription
) implements KafkaEvent {

    public static final String TOPIC = "mopl.playlist.subscription.created";

    @Override
    public String topic() { return TOPIC;}

    public static PlaylistSubscriptionCreatedKafkaEvent of (UUID receiverId, String subscriberName,
                                                            String playlistTitle, String playlistDescription) {
        return new PlaylistSubscriptionCreatedKafkaEvent(receiverId, subscriberName, playlistTitle, playlistDescription);
    }
}
