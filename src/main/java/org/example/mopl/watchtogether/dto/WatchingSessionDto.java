package org.example.mopl.watchtogether.dto;

import lombok.Builder;
import org.example.mopl.content.entity.Content;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingSession;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WatchingSessionDto(
        UUID id,
        Instant createdAt,
        Watcher watcher,
        Content content
) {
    public WatchingSessionDto(WatchingSession watchingSession, Content content){
        this(
                watchingSession.getId(),
                watchingSession.getCreatedAt(),
                watchingSession.getWatcher(),
                content
        );
    }
}
