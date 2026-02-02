package org.example.mopl.content.exception;

import org.example.mopl.common.exception.MoplException;

public class NoSuchTagException extends
    MoplException {

  public NoSuchTagException() {
    super(new NoSuchTagErrorCode());
  }

  public NoSuchTagException(Throwable cause) {
    super(new NoSuchTagErrorCode(), cause);
  }

  public NoSuchTagException(String tagName) {
    super(new NoSuchTagErrorCode());
    addDetail("tagName", tagName);
  }

  public NoSuchTagException(String tagName, Throwable cause) {
    super(new NoSuchTagErrorCode(), cause);
    addDetail("tagName", tagName);
  }

}
