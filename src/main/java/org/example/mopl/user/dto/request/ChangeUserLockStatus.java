package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChangeUserLockStatus {
    @NotNull
    private Boolean locked;
}
