package org.example.mopl.user.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "유저 정보가 없습니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
