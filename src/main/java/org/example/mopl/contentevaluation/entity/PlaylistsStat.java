package org.example.mopl.contentevaluation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.exception.InvalidSubscribeCountDecreaseException;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlists_stats")
public class PlaylistsStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "playlist_id", nullable = false)
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private Playlist playlist;

  @Column(name = "subscribe_count", nullable = false)
  private Long subscribeCount = 0L;

  @Builder(access = AccessLevel.PROTECTED)
  public PlaylistsStat(Playlist playlist) {
    this.playlist = playlist;
  }

  public static PlaylistsStat of(Playlist playlist) {
    return new PlaylistsStat(playlist);
  }

  public void incrementSubscribeCount() {
    this.subscribeCount++;
  }

  public void decrementSubscribeCount() {

    if (this.subscribeCount <= 0) {
      throw new InvalidSubscribeCountDecreaseException(playlist.getTitle());
    }

    this.subscribeCount--;

  }

}
