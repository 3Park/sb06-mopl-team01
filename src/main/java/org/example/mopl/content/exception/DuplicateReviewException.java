package org.example.mopl.content.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class DuplicateReviewException extends MoplException {

  public DuplicateReviewException() {
    super(new DuplicateReviewErrorCode());
  }

  public DuplicateReviewException(String message) {
    super(new DuplicateReviewErrorCode());
    addDetail("message", message);
  }

  public DuplicateReviewException(UUID contentId) {
    super(new DuplicateReviewErrorCode());
    addDetail("contentId", contentId);
  }

}
