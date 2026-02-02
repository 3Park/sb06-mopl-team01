package org.example.mopl.contentevaluation.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchPlaylistErrorCode implements ErrorCode {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getMessage() {
    return "존재하지 않는 플레이리스트입니다.";
  }

  @Override
  public String name() {
    return "No_SUCH_PLAYLIST";
  }
}
