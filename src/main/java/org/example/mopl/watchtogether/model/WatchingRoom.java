package org.example.mopl.watchtogether.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@NoArgsConstructor
public class WatchingRoom {
    private UUID id;
    private Instant createdAt;
    private ConcurrentHashMap<String, WatchingSession> watchers;
    private Content content;

    public WatchingRoom(Content content){
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.watchers = new ConcurrentHashMap<>();
        this.content = content;
    }

    public void addWatcher(WatchingSession watchingSession) {
        watchers.put(watchingSession.getId().toString(), watchingSession);
    }

    public WatchingSession removeWatcher(String sessionId) {
        return watchers.remove(sessionId);
    }

    public long getWatcherCount() {
        return watchers.size();
    }

    public WatchingSession getWatcher(String sessionId){ return this.watchers.get(sessionId);}

    public List<WatchingSession> getWatchers(){return this.watchers.values().stream().toList();}

    public boolean isEmpty() {
        return watchers.isEmpty();
    }
}
