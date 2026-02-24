package org.example.mopl.profile.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {

    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    PROFILE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 프로필만 수정할 수 있습니다."),
    PROFILE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    PROFILE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "프로필 이미지 파일이 필요합니다."),
    PROFILE_IMAGE_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "허용되지 않는 이미지 형식입니다. (jpeg, png)"),
    PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 용량이 제한을 초과했습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 업로드에 실패했습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팔로우 중입니다."),
    FOLLOW_SELF_FORBIDDEN(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우할 수 없습니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

