package org.example.mopl.event.message;

import java.util.UUID;

public record DmMessageReceivedKafkaEvent(
        UUID receiverId,

        String senderName,
        String message
) implements KafkaEvent {

    public static final String TOPIC = "mopl.dm.message.received";

    @Override
    public String topic() { return TOPIC;}

    public static DmMessageReceivedKafkaEvent of(UUID receiverId, String senderName, String message) {
        return new DmMessageReceivedKafkaEvent(receiverId, senderName, message);
    }
}
