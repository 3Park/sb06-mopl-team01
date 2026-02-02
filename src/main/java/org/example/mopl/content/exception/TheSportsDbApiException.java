package org.example.mopl.content.exception;

public class TheSportsDbApiException extends RuntimeException {

  private final int statusCode;
  private final String responseBody;

  public TheSportsDbApiException(int statusCode, String responseBody, String message) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

}
