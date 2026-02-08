package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class S3DeleteFailedException extends MoplException {

  public S3DeleteFailedException() {
    super(new S3DeleteFailedErrorCode());
  }

  public S3DeleteFailedException(Throwable cause) {
    super(new S3DeleteFailedErrorCode(), cause);
  }

  public S3DeleteFailedException(String fileKey) {
    super(new S3DeleteFailedErrorCode());
    addDetail("fileKey", fileKey);
  }

}
