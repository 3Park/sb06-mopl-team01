package org.example.mopl.contentevaluation.exception;

import org.example.mopl.common.exception.ErrorCode;
import org.example.mopl.common.exception.MoplException;

public class ContentEvaluationException extends MoplException {

  public ContentEvaluationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ContentEvaluationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

}
