package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class NoSuchS3ObjectException extends MoplException {

  public NoSuchS3ObjectException(Throwable cause) {
    super(new NoSuchS3ObjectErrorCode(), cause);
  }

  public NoSuchS3ObjectException(String fileKey) {
    super(new NoSuchS3ObjectErrorCode());
    addDetail("fileKey", fileKey);
  }

  public NoSuchS3ObjectException(String message, Throwable cause) {
    super(new NoSuchS3ObjectErrorCode(), cause);
    addDetail("message", message);
  }

  public NoSuchS3ObjectException() {
    super(new NoSuchS3ObjectErrorCode());
  }

}
