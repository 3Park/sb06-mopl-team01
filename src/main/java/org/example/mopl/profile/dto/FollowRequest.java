package org.example.mopl.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** 명세 FollowRequest: followeeId (팔로우 대상 사용자 ID) */
@Getter
@NoArgsConstructor
public class FollowRequest {

    @NotNull(message = "followeeId는 필수입니다.")
    private UUID followeeId;
}
