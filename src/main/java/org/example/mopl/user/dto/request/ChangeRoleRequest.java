package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.example.mopl.user.enums.UserRoleType;

@Getter
public class ChangeRoleRequest {
    @NotNull
    UserRoleType role;
}
