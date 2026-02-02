package org.example.mopl.content.exception;

import java.util.UUID;

public class NoSuchContentException extends ContentException {

  public NoSuchContentException() {
    super(new NoSuchContentErrorCode());
  }

  public NoSuchContentException(Throwable cause) {
    super(new NoSuchContentErrorCode(), cause);
  }

  public NoSuchContentException(String contentUuid) {
    super(new NoSuchContentErrorCode());
    addDetail("contentUuid", contentUuid);
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
