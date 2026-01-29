package org.example.mopl.contentevaluation.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class NoSuchPlaylistException extends MoplException {

  public NoSuchPlaylistException() {
    super(new NoSuchPlaylistErrorCode());
  }

  public NoSuchPlaylistException(Throwable cause) {
    super(new NoSuchPlaylistErrorCode(), cause);
  }

  public NoSuchPlaylistException(String playlistUuid) {
    super(new NoSuchPlaylistErrorCode());
    addDetail("playlistUuid", playlistUuid);
  }

  public NoSuchPlaylistException(String playlistUuid, Throwable cause) {
    super(new NoSuchPlaylistErrorCode(), cause);
    addDetail("playlistUuid", playlistUuid);
  }

  public NoSuchPlaylistException(UUID playlistId) {
    super(new NoSuchPlaylistErrorCode());
    addDetail("playlistId", playlistId.toString());
  }

  public NoSuchPlaylistException(UUID playlistId, Throwable cause) {
    super(new NoSuchPlaylistErrorCode(), cause);
    addDetail("playlistId", playlistId.toString());
  }
}
