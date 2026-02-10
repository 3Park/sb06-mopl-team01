package org.example.mopl.profile.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowerCountResponse {

    private final long count;
}
