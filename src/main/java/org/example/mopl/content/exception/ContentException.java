package org.example.mopl.content.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.example.mopl.common.exception.MoplException;

public class ContentException extends MoplException {

  public ContentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ContentException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

}
