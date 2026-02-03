package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    @NotBlank(message = "변경할 비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 20, message = "8자 이상 20자 이하 이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W_]).*$",
            message = "영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;
}
