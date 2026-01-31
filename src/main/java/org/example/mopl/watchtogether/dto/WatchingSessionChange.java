package org.example.mopl.watchtogether.dto;

import lombok.Builder;
import org.example.mopl.watchtogether.enumeration.ChangeType;

@Builder
public record WatchingSessionChange(
        ChangeType type,
        WatchingSessionDto watchingSession,
        long watcherCount
) {
}
