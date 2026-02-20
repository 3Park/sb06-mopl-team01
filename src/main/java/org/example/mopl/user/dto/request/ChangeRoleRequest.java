package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.mopl.user.enums.UserRoleType;

public record ChangeRoleRequest(
        @NotNull
        UserRoleType role
) {

}
