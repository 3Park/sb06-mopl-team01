package org.example.mopl.event.message;

import java.util.List;
import java.util.UUID;

public record PlaylistCreatedKafkaEvent(
        List<UUID> receiverIds,

        String creatorName,
        String playlistTitle,
        String playlistDescription
) implements KafkaEvent {

    public static final String TOPIC = "mopl.playlist.created";

    @Override
    public String topic() { return TOPIC;}

    public static PlaylistCreatedKafkaEvent of (List<UUID> followerIds, String creatorName, String playlistTitle, String playlistDescription) {
        return new PlaylistCreatedKafkaEvent(followerIds, creatorName, playlistTitle, playlistDescription);
    }
}
