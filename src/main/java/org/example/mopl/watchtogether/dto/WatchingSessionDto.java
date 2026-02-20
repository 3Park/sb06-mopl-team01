package org.example.mopl.watchtogether.dto;

import lombok.Builder;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingSession;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WatchingSessionDto(
        String id,
        Instant createdAt,
        Watcher watcher,
        ContentDto content
) {
    public WatchingSessionDto(WatchingSession watchingSession, ContentDto content){
        this(
                watchingSession.getId(),
                watchingSession.getCreatedAt(),
                watchingSession.getWatcher(),
                content
        );
    }
}
