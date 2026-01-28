package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchReviewErrorCode implements ErrorCode  {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getMessage() {
    return "존재하지 않는 리뷰입니다.";
  }

  @Override
  public String name() {
    return "NO_SUCH_REVIEW";
  }
}
