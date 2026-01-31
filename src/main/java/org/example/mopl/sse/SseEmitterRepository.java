package org.example.mopl.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(UUID receiverId, SseEmitter emitter) {
        return emitters.put(receiverId, emitter);
    }

    public void deleteById(UUID receiverId) {
        emitters.remove(receiverId);
    }

    public SseEmitter findById(UUID receiverId) {
        return emitters.get(receiverId);
    }

    public Map<UUID, SseEmitter> findAll() { return emitters; }
}
