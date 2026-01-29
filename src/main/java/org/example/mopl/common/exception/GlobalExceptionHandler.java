package org.example.mopl.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MoplException.class)
    public ResponseEntity<ErrorResponse> handleMoplException(MoplException ex) {
        log.error("커스텀 예외 발생: code={}, message={}", ex.getErrorCode(), ex.getMessage(), ex);
        HttpStatus httpStatus = ex.getErrorCode().getHttpStatus();
        ErrorResponse errorResponse = new ErrorResponse(ex, httpStatus.value());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
