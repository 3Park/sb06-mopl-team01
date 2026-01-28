package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class NoSuchTagException extends MoplException {
  public NoSuchTagException(String message) {
    super(message);
  }
}
