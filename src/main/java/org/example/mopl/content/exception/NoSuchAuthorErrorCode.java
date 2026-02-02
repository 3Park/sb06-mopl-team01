package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchAuthorErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getMessage() {
    return "Invalid author information.";
  }

  @Override
  public String name() {
    return "INVALID_AUTHOR";
  }
}
