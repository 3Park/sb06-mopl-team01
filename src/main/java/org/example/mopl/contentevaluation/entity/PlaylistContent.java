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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlist_contents")
public class PlaylistContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "content_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Content content;

  @JoinColumn(name = "playlist_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Playlist playlist;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Builder(access = AccessLevel.PROTECTED)
  public PlaylistContent(Content content, Playlist playlist) {
    this.content = content;
    this.playlist = playlist;
  }

  public static PlaylistContent of(Content content, Playlist playlist) {
    return PlaylistContent.builder()
        .content(content)
        .playlist(playlist)
        .build();
  }

}
