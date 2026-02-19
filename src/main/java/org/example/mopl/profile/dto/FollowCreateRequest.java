package org.example.mopl.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowCreateRequest {

    @NotNull(message = "followeeId는 필수입니다.")
    private Long followeeId;
}
