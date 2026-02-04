package org.example.mopl.content.exception;

public class RetryableTheSportsDbApiException extends TheSportsDbApiException{

  public RetryableTheSportsDbApiException(int statusCode, String responseBody, String message) {
    super(statusCode, responseBody, message);
  }

  public static RetryableTheSportsDbApiException createIfRetryable(TheSportsDbApiException original) {
    if (isRetryableStatusCode(original.getStatusCode())) {
      return new RetryableTheSportsDbApiException(original.getStatusCode(), original.getResponseBody(), original.getMessage());
    }
    throw original; // 재시료하지 않을 상태 코드는 원본 예외 발생
  }

  private static boolean isRetryableStatusCode(int statusCode) {
    return statusCode == 408 || // Request Timeout
        statusCode == 429 || // Too Many Requests
        statusCode == 500 || // Internal Server Error
        statusCode == 502 || // Bad Gateway
        statusCode == 503 || // Service Unavailable
        statusCode == 504;   // Gateway Timeout
  }

}
