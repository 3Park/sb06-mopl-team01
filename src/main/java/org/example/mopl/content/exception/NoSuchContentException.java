package org.example.mopl.content.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class NoSuchContentException extends MoplException {

  public NoSuchContentException() {
    super(new NoSuchContentErrorCode());
  }

  public NoSuchContentException(Throwable cause) {
    super(new NoSuchContentErrorCode(), cause);
  }

  public NoSuchContentException(Long contentId) {
    super(new NoSuchContentErrorCode());
    addDetail("contentUuid", contentId);
  }

  public NoSuchContentException(UUID contentUuid) {
    super(new NoSuchContentErrorCode());
    addDetail("contentUuid", contentUuid.toString());
  }

  public NoSuchContentException(String contentUuid, Throwable cause) {
    super(new NoSuchContentErrorCode(), cause);
    addDetail("contentUuid", contentUuid);
  }

}
