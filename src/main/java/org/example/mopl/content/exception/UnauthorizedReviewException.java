package org.example.mopl.content.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class UnauthorizedReviewException extends MoplException {

  public UnauthorizedReviewException() {
    super(new UnauthorizedReview());
  }

  public UnauthorizedReviewException(Throwable cause) {
    super(new UnauthorizedReview(), cause);
  }

  public UnauthorizedReviewException(String email) {
    super(new UnauthorizedReview());
    addDetail("email", email);
  }

  public UnauthorizedReviewException(String email, Throwable cause) {
    super(new UnauthorizedReview(), cause);
    addDetail("email", email);
  }

  public UnauthorizedReviewException(String email, UUID reviewId) {
    super(new UnauthorizedReview());
    addDetail("email", email);
    addDetail("reviewId", reviewId.toString());
  }

  public UnauthorizedReviewException(String email, UUID reviewId, Throwable cause) {
    super(new UnauthorizedReview(), cause);
    addDetail("email", email);
    addDetail("reviewId", reviewId.toString());
  }
}
