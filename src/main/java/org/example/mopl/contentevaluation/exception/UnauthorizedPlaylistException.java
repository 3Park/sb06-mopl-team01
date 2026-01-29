package org.example.mopl.contentevaluation.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class UnauthorizedPlaylistException extends MoplException {

  public UnauthorizedPlaylistException() {
    super(new UnauthorizedPlaylistErrorCode());
  }

  public UnauthorizedPlaylistException(Throwable cause) {
    super(new UnauthorizedPlaylistErrorCode(), cause);
  }

  public UnauthorizedPlaylistException(String email) {
    super(new UnauthorizedPlaylistErrorCode());
    addDetail("email", email);
  }

  public UnauthorizedPlaylistException(String email, Throwable cause) {
    super(new UnauthorizedPlaylistErrorCode(), cause);
    addDetail("email", email);
  }

  public UnauthorizedPlaylistException(String email, UUID playlistId) {
    super(new UnauthorizedPlaylistErrorCode());
    addDetail("email", email);
    addDetail("playlistId", playlistId.toString());
  }

  public UnauthorizedPlaylistException(String email, UUID playlistId, Throwable cause) {
    super(new UnauthorizedPlaylistErrorCode(), cause);
    addDetail("email", email);
    addDetail("playlistId", playlistId.toString());
  }
}
