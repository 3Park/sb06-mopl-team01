package org.example.mopl.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    DUPLICATED_USER(HttpStatus.BAD_REQUEST,"이미 가입한 유저 입니다."),
    INVALID_ROLE(HttpStatus.UNAUTHORIZED,"권한이 없습니다"),
    INVALID_DATA(HttpStatus.BAD_REQUEST,"잘못된 데이터 입니다."),
    INVALID_USER(HttpStatus.NOT_FOUND,"없는 유저입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
