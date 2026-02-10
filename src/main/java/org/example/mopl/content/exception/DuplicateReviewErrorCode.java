package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateReviewErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getMessage() {
    return "이미 리뷰를 작성한 콘텐츠입니다.";
  }

  @Override
  public String name() {
    return "DUPLICATE_REVIEW";
  }
}
