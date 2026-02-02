package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedReviewErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  @Override
  public String getMessage() {
    return "리뷰 접근 권한이 없습니다.";
  }

  @Override
  public String name() {
    return "UNAUTHORIZED_REVIEW";
  }
}
