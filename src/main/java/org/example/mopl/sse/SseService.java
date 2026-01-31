package org.example.mopl.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.notification.dto.NotificationDto;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;

    @Value("${sse.timeout:3600000}")
    private long timeout;

    public SseEmitter subscribe(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(timeout);

        emitter.onCompletion(() -> sseEmitterRepository.deleteById(receiverId));
        emitter.onTimeout(() -> {sseEmitterRepository.deleteById(receiverId);});
        emitter.onError((e) -> {sseEmitterRepository.deleteById(receiverId);});

        sseEmitterRepository.save(receiverId, emitter);

        // lastEventId 존재 + 놓친 데이터가 있을 경우 보내주기
        if (lastEventId != null) {
            Notification lastNotification = notificationRepository.findByUuid(lastEventId).orElse(null);

            if (lastNotification != null) {
                try {
                    PageRequest limit = PageRequest.of(0, 20,
                            Sort.by("createdAt").ascending().and(Sort.by("id").ascending()));

                    List<Notification> missed = notificationRepository.findAllByReceiverIdAndCreatedAtAfter(
                            receiverId, lastNotification.getCreatedAt(), limit
                    );

                    for (Notification notification : missed) {
                        sendNotification(receiverId, NotificationDto.from(notification));
                    }
                } catch (Exception e) {
                    log.warn("재전송 실패 (연결은 유지): {}", e.getMessage());
                }
            }
        }
        ping(emitter);

        return emitter;
    }

    public void sendNotification(UUID receiverId, NotificationDto data) {
        send(receiverId, "notification", data, data.id().toString());
    }

    private void send(UUID receiverId, String eventName, Object data, String eventId) {
        SseEmitter emitter = sseEmitterRepository.findById(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(eventId)
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                sseEmitterRepository.deleteById(receiverId);
                log.warn("SSE 전송 실패: {}", receiverId);
            }
        }
    }

    @Scheduled(fixedDelay = 1000 * 60)
    public void cleanUp() {
        sseEmitterRepository.findAll().forEach((receiverId, sseEmitter) -> {
                    if(!ping(sseEmitter)) {
                        sseEmitterRepository.deleteById(receiverId);
                        log.info("Ping 실패로 인한 연결 삭제: receiverId={}", receiverId);
                    }
                });
    }

    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("ping"));
            return true;
        } catch (IOException e) {
            log.warn("Ping 전송 실패", e);
            return false;
        }
    }
}
