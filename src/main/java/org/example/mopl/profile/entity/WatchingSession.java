package org.example.mopl.profile.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.example.mopl.user.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "watching_sessions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_watching_sessions_watcher_content", columnNames = {"watcher_id", "content_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watcher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watching_sessions_watcher"))
    private User watcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watching_sessions_content"))
    private Content content;

    @Column(name = "last_position", nullable = false)
    private Long lastPosition = 0L;

    @Column(name = "duration")
    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WatchingStatus status = WatchingStatus.WATCHING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum WatchingStatus {
        WATCHING, PAUSED, ENDED
    }

    @PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
