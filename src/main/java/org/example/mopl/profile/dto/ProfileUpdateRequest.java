package org.example.mopl.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateRequest {

    /** 이름: 1~100자 (공백만 있으면 안 됨). PATCH 시 생략 가능. */
    @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하여야 합니다.")
    private String name;

    /** 프로필 이미지 URL: 값이 있으면 http(s) 형식, 최대 1000자. null/빈 문자열은 허용(이미지 제거). */
    @Size(max = 1000)
    @Pattern(
        regexp = "^(https?://).*|^$",
        message = "프로필 이미지 URL은 http:// 또는 https://로 시작해야 합니다."
    )
    private String profileImageUrl;
}
