package org.example.mopl.content.exception;

public class NoSuchReviewException extends ContentException {

  public NoSuchReviewException() {
    super(new NoSuchReviewErrorCode());
  }

  public NoSuchReviewException(Throwable cause) {
    super(new NoSuchReviewErrorCode(), cause);
  }

  public NoSuchReviewException(String reviewUuid) {
    super(new NoSuchReviewErrorCode());
    addDetail("reviewUuid", reviewUuid);
  }

  public NoSuchReviewException(String reviewUuid, Throwable cause) {
    super(new NoSuchReviewErrorCode(), cause);
    addDetail("reviewUuid", reviewUuid);
  }

}
