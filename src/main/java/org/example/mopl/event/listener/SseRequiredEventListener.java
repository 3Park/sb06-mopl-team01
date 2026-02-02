package org.example.mopl.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.event.message.NotificationCreatedEvent;
import org.example.mopl.sse.SseService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredEventListener {

    private final SseService sseService;

    @Async
    @TransactionalEventListener
    public void on(NotificationCreatedEvent event) {
        UUID receiverId = event.notificationDto().receiverId();
        sseService.sendNotification(receiverId, event.notificationDto());
    }
}
