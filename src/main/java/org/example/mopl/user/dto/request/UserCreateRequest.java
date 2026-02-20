package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record UserCreateRequest(
        @NotBlank(message = "이름은 필수 입니다.")
        String name,

        @NotBlank
        @Email(message = "이메일 양식을 확인하세요.")
        String email,

        @NotBlank
        @Size(min = 8, max = 20, message = "8자 이상 20자 이하 이어야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W_]).*$",
                message = "영문, 숫자, 특수문자를 포함해야 합니다."
        )
         String password
) {
}
