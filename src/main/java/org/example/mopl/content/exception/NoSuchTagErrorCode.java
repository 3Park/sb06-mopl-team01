package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchTagErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getMessage() {
    return "존재하지 않는 태그입니다.";
  }

  @Override
  public String name() {
    return "No_SUCH_TAG";
  }
}
