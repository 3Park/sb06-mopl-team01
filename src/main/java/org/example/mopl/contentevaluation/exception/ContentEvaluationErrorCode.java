package org.example.mopl.contentevaluation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContentEvaluationErrorCode {

  INVALID_SUBSCRIBE_COUNT_DECREASE(HttpStatus.BAD_REQUEST, "잘못된 구독자 수 감소 요청입니다."),
  NO_SUCH_PLAYLIST(HttpStatus.NOT_FOUND, "존재하지 않는 플레이리스트입니다."),
  UNAUTHORIZED_PLAYLIST(HttpStatus.FORBIDDEN, "플레이리스트에 대한 권한이 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

}
