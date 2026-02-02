package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRatingDecreaseErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getMessage() {
    return "잘못된 리뷰 감소 요청입니다.";
  }

  @Override
  public String name() {
    return "INVALID_RATING_DECREASE";
  }
}
