package org.example.mopl.watchtogether.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.content.entity.Content;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WatchingRoom {
    private UUID id;
    private Instant createdAt;
    private ConcurrentHashMap<String, Watcher> watchers;
    private Content content;

    public WatchingRoom(Content content){
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.watchers = new ConcurrentHashMap<>();
        this.content = content;
    }

    public void addWatcher(Watcher watcher) {watchers.put(watcher.id.toString(), watcher);}

    public Watcher removeWatcher(String watcherId) {
        return watchers.remove(watcherId);
    }

    public long getWatcherCount() {
        return watchers.size();
    }

    public boolean isEmpty() {
        return watchers.isEmpty();
    }
}
