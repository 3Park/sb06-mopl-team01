package org.example.mopl.directmessage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DirectMessageErrorCode implements ErrorCode {

    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 대화방입니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 참여자입니다."),
    DIRECT_MESSAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "메시지에 대한 접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;


    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
