package org.example.mopl.content.exception;

import java.util.UUID;
import org.example.mopl.common.exception.MoplException;

public class NoSuchAuthorException extends MoplException {

  public NoSuchAuthorException() {
    super(new NoSuchAuthorErrorCode());
  }

  public NoSuchAuthorException(Throwable cause) {
    super(new NoSuchAuthorErrorCode(), cause);
  }

  public NoSuchAuthorException(String message) {
    super(new NoSuchAuthorErrorCode());
    addDetail("message", message);
  }

  public NoSuchAuthorException(String message, Throwable cause) {
    super(new NoSuchAuthorErrorCode(), cause);
    addDetail("message", message);
  }

  public NoSuchAuthorException(Long userId, String message) {
    super(new NoSuchAuthorErrorCode());
    addDetail("userId", userId);
    addDetail("message", message);
  }

  public NoSuchAuthorException(UUID userId) {
    super(new NoSuchAuthorErrorCode());
    addDetail("userId", userId);
  }

  public NoSuchAuthorException(UUID userId, String message) {
    super(new NoSuchAuthorErrorCode());
    addDetail("userId", userId);
    addDetail("message", message);
  }

  public NoSuchAuthorException(Long userId, String message, Throwable cause) {
    super(new NoSuchAuthorErrorCode(), cause);
    addDetail("userId", userId);
    addDetail("message", message);
  }

  public NoSuchAuthorException(UUID userId, String message, Throwable cause) {
    super(new NoSuchAuthorErrorCode(), cause);
    addDetail("userId", userId);
    addDetail("message", message);
  }

}
