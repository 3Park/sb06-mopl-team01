package org.example.mopl.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class FollowCreateRequest {

    @NotNull(message = "followeeUuid는 필수입니다.")
    private UUID followeeUuid;
}
