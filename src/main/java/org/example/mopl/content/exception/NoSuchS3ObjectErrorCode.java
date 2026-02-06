package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchS3ObjectErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getMessage() {
    return "S3 객체를 찾을 수 없습니다.";
  }

  @Override
  public String name() {
    return "No_SUCH_S3_OBJECT";
  }
}
