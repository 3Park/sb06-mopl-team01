package org.example.mopl.profile.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * 알림/DM 등에서 사용할 사용자 프로필 요약 정보.
 */
@Getter
@Builder
public class UserSummary {

    private final Long userId;
    private final UUID userUuid;
    private final String name;
    private final String profileImageUrl;
}
