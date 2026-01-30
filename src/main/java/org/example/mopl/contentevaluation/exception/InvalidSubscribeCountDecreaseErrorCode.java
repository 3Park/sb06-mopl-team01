package org.example.mopl.contentevaluation.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidSubscribeCountDecreaseErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getMessage() {
    return "잘못된 구독자 수 감소 요청입니다.";
  }

  @Override
  public String name() {
    return "INVALID_SUBSCRIBE_COUNT_DECREASE";
  }
}
