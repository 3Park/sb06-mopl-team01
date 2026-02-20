package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeUserLockStatus(
        @NotNull
        Boolean locked
) {

}
