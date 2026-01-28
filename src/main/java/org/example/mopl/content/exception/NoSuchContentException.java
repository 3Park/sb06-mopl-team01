package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.example.mopl.common.exception.MoplException;

public class NoSuchContentException extends MoplException {

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

  public NoSuchContentException(String contentUuid, Throwable cause) {
    super(new NoSuchContentErrorCode(), cause);
    addDetail("contentUuid", contentUuid);
  }

}
