package org.example.mopl.profile.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.user.entity.User;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_follows", columnNames = {"follower_id", "followee_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    private Long id;

    @UuidGenerator
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @JoinColumn(name = "follower_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User follower;

    @JoinColumn(name = "followee_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User followee;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder(access = AccessLevel.PROTECTED)
    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }

    public static Follow of(User follower, User followee) {
        return Follow.builder()
            .follower(follower)
            .followee(followee)
            .build();
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
