package org.example.mopl.contentevaluation.event;

import org.example.mopl.contentevaluation.entity.Playlist;

public record CreatePlaylistEvent(
    Playlist playlist
) {

  public static CreatePlaylistEvent of(Playlist playlist) {
    return new CreatePlaylistEvent(playlist);
  }

}
