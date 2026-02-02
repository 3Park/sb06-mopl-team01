package org.example.mopl.user.exception;

import org.example.mopl.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        UserException exception = new UserException(UserErrorCode.INVALID_DATA);
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> exception.addDetail(e.getField(),e.getDefaultMessage()));

        ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
