package org.example.mopl.profile.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 프로필 이미지 업로드 설정.
 * application.yaml 또는 환경 변수로 override 가능.
 * 예: PROFILE_IMAGE_MAX_SIZE=10485760, PROFILE_IMAGE_ALLOWED_CONTENT_TYPES_0=image/jpeg
 */
@Component
@ConfigurationProperties(prefix = "profile.image")
public class ProfileImageProperties {

    /** 최대 파일 크기(바이트). 기본 5MB */
    private long maxSize = 5 * 1024 * 1024L;

    /** 허용 Content-Type 목록. 기본 jpeg, png */
    private List<String> allowedContentTypes = List.of("image/jpeg", "image/png");

    /** S3 객체 키 접두사. 기본 profile/ */
    private String keyPrefix = "profile/";

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes != null ? allowedContentTypes : List.of();
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix != null ? keyPrefix : "profile/";
    }
}
