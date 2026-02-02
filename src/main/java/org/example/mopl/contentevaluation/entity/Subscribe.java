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

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscribes")
public class Subscribe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @UuidGenerator
  @Column(name = "uuid", nullable = false, unique = true)
  private UUID uuid;

  @JoinColumn(name = "user_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  @JoinColumn(name = "playlist_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Playlist playlist;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Builder(access = AccessLevel.PROTECTED)
  public Subscribe(User user, Playlist playlist) {
    this.user = user;
    this.playlist = playlist;
  }

  public static Subscribe of(User user, Playlist playlist) {
    return Subscribe.builder()
        .user(user)
        .playlist(playlist)
        .build();
  }

}
