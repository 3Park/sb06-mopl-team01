package org.example.mopl.event.message;

import java.util.UUID;

public record PlaylistCreatedKafkaEvent(
        UUID receiverId,

        String creatorName,
        String playlistTitle,
        String playlistDescription
) implements KafkaEvent {

    public static final String TOPIC = "mopl.playlist.created";

    @Override
    public String topic() { return TOPIC;}

    public static PlaylistCreatedKafkaEvent of (UUID receiverId, String creatorName, String playlistTitle, String playlistDescription) {
        return new PlaylistCreatedKafkaEvent(receiverId, creatorName, playlistTitle, playlistDescription);
    }
}

