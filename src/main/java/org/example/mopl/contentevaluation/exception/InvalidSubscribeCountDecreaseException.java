package org.example.mopl.contentevaluation.exception;

import org.example.mopl.common.exception.MoplException;

public class InvalidSubscribeCountDecreaseException extends MoplException {

  public InvalidSubscribeCountDecreaseException() {
    super(new InvalidSubscribeCountDecreaseErrorCode());
  }

  public InvalidSubscribeCountDecreaseException(Throwable cause) {
    super(new InvalidSubscribeCountDecreaseErrorCode(), cause);
  }

  public InvalidSubscribeCountDecreaseException(String message) {
    super(new InvalidSubscribeCountDecreaseErrorCode());
    addDetail("message", message);
  }

  public InvalidSubscribeCountDecreaseException(String message, Throwable cause) {
    super(new InvalidSubscribeCountDecreaseErrorCode(), cause);
    addDetail("message", message);
  }
}
