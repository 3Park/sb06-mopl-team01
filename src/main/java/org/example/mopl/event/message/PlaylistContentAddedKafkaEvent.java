package org.example.mopl.event.message;

import java.util.UUID;

public record PlaylistContentAddedKafkaEvent(
        UUID receiverId,

        String playlistTitle,
        String contentTitle
) implements KafkaEvent {

    public static final String TOPIC = "mopl.playlist.content.added";

    @Override
    public String topic() {
        return TOPIC;
    }

    public static PlaylistContentAddedKafkaEvent of (UUID receiverId, String playlistTitle, String contentTitle) {
        return new PlaylistContentAddedKafkaEvent(receiverId, playlistTitle, contentTitle);
    }
}

