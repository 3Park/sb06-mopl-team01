package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class S3UploadFailedException extends MoplException {

  public S3UploadFailedException() {
    super(new S3UploadFailedErrorCode());
  }

  public S3UploadFailedException(Throwable cause) {
    super(new S3UploadFailedErrorCode(), cause);
  }

  public S3UploadFailedException(String fileKey) {
    super(new S3UploadFailedErrorCode());
    addDetail("fileKey", fileKey);
  }

}
