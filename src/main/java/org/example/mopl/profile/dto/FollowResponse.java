package org.example.mopl.profile.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowResponse {

    private final UUID followUuid;
}
