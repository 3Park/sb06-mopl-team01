package org.example.mopl.content.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContentErrorCode {

  DUPLICATE_REVIEW(HttpStatus.BAD_REQUEST, "이미 리뷰를 작성한 콘텐츠입니다."),
  INVALID_RATING_DECREASE(HttpStatus.BAD_REQUEST, "잘못된 리뷰 감소 요청입니다."),
  NO_SUCH_AUTHOR(HttpStatus.NOT_FOUND, "존재하지 않는 저자입니다."),
  NO_SUCH_CONTENT(HttpStatus.NOT_FOUND, "존재하지 않는 콘텐츠입니다."),
  NO_SUCH_REVIEW(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
  NO_SUCH_S3_OBJECT(HttpStatus.NOT_FOUND, "S3 객체를 찾을 수 없습니다."),
  NO_SUCH_TAG(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
  S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 삭제에 실패했습니다."),
  S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 업로드에 실패했습니다."),
  UNAUTHORIZED_REVIEW(HttpStatus.UNAUTHORIZED, "리뷰 작성 권한이 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

}
