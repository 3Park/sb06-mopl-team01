package org.example.mopl.contentevaluation.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedPlaylistErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  @Override
  public String getMessage() {
    return "해당 플레이리스트에 접근할 권한이 없습니다.";
  }

  @Override
  public String name() {
    return "UNAUTHORIZED_PLAYLIST_ACCESS";
  }
}
