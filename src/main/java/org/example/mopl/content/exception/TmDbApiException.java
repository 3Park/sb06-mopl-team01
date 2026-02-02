package org.example.mopl.content.exception;

import lombok.Getter;

@Getter
public class TmDbApiException extends RuntimeException {

  private final int statusCode;
  private final String responseBody;

  public TmDbApiException(int statusCode, String responseBody, String message) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

}
