package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class NoSuchReviewException extends
    MoplException {

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
