package org.example.mopl.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** 프로필 페이지 - 현재 시청 중인 콘텐츠 한 건 (제목, 썸네일 등 포함) */
@Getter
@Builder
public class WatchingContentDto {

    private UUID sessionUuid;
    private Long contentId;
    private UUID contentUuid;
    private String contentTitle;
    private String contentDescription;
    private String contentThumbnailUrl;
    private String contentType;
    private Long lastPosition;
    private Long duration;
    private String status;
    private Instant updatedAt;
}
