package org.example.mopl.watchtogether.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class WatchingSession {
    private String id;
    private Instant createdAt;
    private Watcher watcher;

    public WatchingSession(String id,Watcher watcher){
        this.id = id;
        this.createdAt = Instant.now();
        this.watcher = watcher;
    }
}
