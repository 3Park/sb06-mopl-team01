package org.example.mopl.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.event.message.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener { // KafkaProducer

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener
    public void sendToKafka(KafkaEvent kafkaEvent) {
        try {
            String payload = objectMapper.writeValueAsString(kafkaEvent);
            kafkaTemplate.send(kafkaEvent.topic(), payload);
            log.info("Kafka 발송 성공 [Topic:{}]", kafkaEvent.topic());
        } catch (JsonProcessingException e) {
            log.error("Kafka 발송 실패 [Topic:{}]", kafkaEvent.topic());
            throw new RuntimeException(e);
        }
    }
}
