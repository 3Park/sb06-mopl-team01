package org.example.mopl.event.message;

import java.util.UUID;

public interface KafkaEvent {
    String topic();
    UUID receiverId();
}
