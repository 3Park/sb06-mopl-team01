package org.example.mopl.auth.exception;

import org.example.mopl.common.exception.ErrorResponse;
import org.example.mopl.user.exception.UserErrorCode;
import org.example.mopl.user.exception.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleValidation(AuthorizationDeniedException ex) {

        AuthException exception = new AuthException(UserErrorCode.INVALID_ROLE);
        ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
