package org.example.mopl.watchtogether.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WatchTogetherErrorCode implements ErrorCode {

    NO_VIEWERS(HttpStatus.BAD_REQUEST,"해당 시청자 없음")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
