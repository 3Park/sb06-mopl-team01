package org.example.mopl.profile.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
    name = "profiles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_profiles_user_id", columnNames = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_profiles_user_id")
    )
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Profile(
        UUID uuid,
        User user,
        String name,
        String profileImageUrl
    ) {
        this.uuid = uuid;
        this.user = user;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = LocalDateTime.now();
    }

    // 프로필 정보 수정
    public void update(String name, String profileImageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // 이름 수정
    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // 프로필 이미지 URL 수정
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    // 엔티티 생성 전 UUID 자동 생성
    @PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }


    // 엔티티 업데이트 전 updatedAt 자동 갱신
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
