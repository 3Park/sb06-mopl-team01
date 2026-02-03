package org.example.mopl.user.dto.request;

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
        Integer limit,
        UserSortDirection sortDirection,
        UserSortBy sortBy
) {
}
