package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class S3UploadFailedErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getMessage() {
    return "S3 업로드에 실패했습니다.";
  }

  @Override
  public String name() {
    return "S3_UPLOAD_FAILED";
  }
}
