package org.example.mopl.contentevaluation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.user.entity.User;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlists")
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @UuidGenerator
  @Column(name = "uuid", nullable = false, unique = true)
  private UUID uuid;

  @Column(name = "title", nullable = false)
  private String title;

  @JoinColumn(name = "user_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder(access = AccessLevel.PROTECTED)
  public Playlist(String title, User user) {
    this.title = title;
    this.user = user;
  }

  public static Playlist of(String title, User user) {
    return Playlist.builder()
        .title(title)
        .user(user)
        .build();
  }

}
