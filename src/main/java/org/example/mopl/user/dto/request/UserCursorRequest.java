package org.example.mopl.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.mopl.user.enums.UserRoleType;
import org.example.mopl.user.enums.UserSortBy;
import org.example.mopl.user.enums.UserSortDirection;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public record UserCursorRequest(
        String emailLike,
        UserRoleType roleEqual,
        Boolean isLocked,
        String cursor,
        UUID idAfter,

        @NotNull
        Integer limit,
        @NotBlank
        UserSortDirection sortDirection,
        @NotBlank
        UserSortBy sortBy
) {
}
