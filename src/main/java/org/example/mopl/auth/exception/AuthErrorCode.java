package org.example.mopl.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "유저 정보가 없습니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "ID / Password를 확인하세요."),
    INVALID_EMAIL(HttpStatus.NOT_FOUND,"없는 이메일 주소 입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
