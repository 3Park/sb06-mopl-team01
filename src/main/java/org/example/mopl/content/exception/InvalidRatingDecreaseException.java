package org.example.mopl.content.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class InvalidRatingDecreaseException extends MoplException {

  public InvalidRatingDecreaseException() {
    super(new InvalidRatingDecreaseErrorCode());
  }

  public InvalidRatingDecreaseException(Throwable cause) {
    super(new InvalidRatingDecreaseErrorCode(), cause);
  }

  public InvalidRatingDecreaseException(String message) {
    super(new InvalidRatingDecreaseErrorCode());
    addDetail("message", message);
  }

  public InvalidRatingDecreaseException(String message, Throwable cause) {
    super(new InvalidRatingDecreaseErrorCode(), cause);
    addDetail("message", message);
  }

  public InvalidRatingDecreaseException(Long contentId, String message) {
    super(new InvalidRatingDecreaseErrorCode());
    addDetail("contentId", contentId);
    addDetail("message", message);
  }

  public InvalidRatingDecreaseException(UUID contentId) {
    super(new InvalidRatingDecreaseErrorCode());
    addDetail("contentId", contentId);
  }

  public InvalidRatingDecreaseException(UUID contentId, String message) {
    super(new InvalidRatingDecreaseErrorCode());
    addDetail("contentId", contentId);
    addDetail("message", message);
  }

  public InvalidRatingDecreaseException(Long contentId, String message, Throwable cause) {
    super(new InvalidRatingDecreaseErrorCode(), cause);
    addDetail("contentId", contentId);
    addDetail("message", message);
  }

  public InvalidRatingDecreaseException(UUID contentId, String message, Throwable cause) {
    super(new InvalidRatingDecreaseErrorCode(), cause);
    addDetail("contentId", contentId);
    addDetail("message", message);
  }

}
