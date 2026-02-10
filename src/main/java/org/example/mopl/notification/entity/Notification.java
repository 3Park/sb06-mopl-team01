package org.example.mopl.notification.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.notification.enums.Level;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification {

    @Id
    @Tsid
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(nullable = false, updatable = false)
    private Level level;

    @Column(name = "receiver_id", nullable = false, updatable = false)
    private UUID receiverId;

    @Column(nullable = false, updatable = false)
    private String title;

    @Column(updatable = false)
    private String content;


    public static Notification of(UUID receiverId, String title, String content, Level level) {
        return Notification.builder().receiverId(receiverId)
                .title(title).content(content).level(level).build();
    }

    @Builder
    private Notification(Level level, UUID receiverId, String title, String content) {
        this.uuid = UUID.randomUUID();

        this.level = level;
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
    }

    public boolean isSameReceiverId(UUID userId) {
        return receiverId.equals(userId);
    }
}
