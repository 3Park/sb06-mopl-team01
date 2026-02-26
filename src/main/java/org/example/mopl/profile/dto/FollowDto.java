package org.example.mopl.profile.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/** 명세 FollowDto: id(팔로우 ID), followeeId(팔로우 대상), followerId(팔로워) */
@Getter
@Builder
public class FollowDto {

    private final UUID id;
    private final UUID followeeId;
    private final UUID followerId;
}
