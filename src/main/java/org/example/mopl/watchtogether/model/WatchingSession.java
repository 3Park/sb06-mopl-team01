package org.example.mopl.watchtogether.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class WatchingSession {
    private UUID id;
    private Instant createdAt;
    private Watcher watcher;

    public WatchingSession(String id,Watcher watcher){
        this.id = UUID.fromString(id);
        this.createdAt = Instant.now();
        this.watcher = watcher;
    }
}
