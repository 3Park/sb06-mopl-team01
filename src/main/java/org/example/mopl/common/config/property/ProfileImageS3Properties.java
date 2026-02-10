package org.example.mopl.common.config.property;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.profile.s3")
public class ProfileImageS3Properties {

    private String profileImagePrefix = "profile/";
    private long profileImageMaxSizeBytes = 5 * 1024 * 1024; // 5MB
    private List<String> profileImageAllowedContentTypes = List.of("image/jpeg", "image/png");

    public String getProfileImagePrefix() {
        return profileImagePrefix;
    }

    public void setProfileImagePrefix(String profileImagePrefix) {
        this.profileImagePrefix = profileImagePrefix;
    }

    public long getProfileImageMaxSizeBytes() {
        return profileImageMaxSizeBytes;
    }

    public void setProfileImageMaxSizeBytes(long profileImageMaxSizeBytes) {
        this.profileImageMaxSizeBytes = profileImageMaxSizeBytes;
    }

    public List<String> getProfileImageAllowedContentTypes() {
        return profileImageAllowedContentTypes;
    }

    public void setProfileImageAllowedContentTypes(List<String> profileImageAllowedContentTypes) {
        this.profileImageAllowedContentTypes = profileImageAllowedContentTypes;
    }
}

