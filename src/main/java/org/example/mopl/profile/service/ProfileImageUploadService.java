package org.example.mopl.profile.service;

import org.example.mopl.common.exception.MoplException;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.exception.ProfileErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileImageUploadService {

    private static final String PROFILE_IMAGE_PREFIX = "profile/";
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png"
    );

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static:ap-northeast-2}")
    private String region;

    public ProfileImageUploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(Profile profile, MultipartFile file) throws IOException {
        validate(file);

        String ext = CONTENT_TYPE_TO_EXT.getOrDefault(
                Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(),
                "jpg"
        );
        String key = PROFILE_IMAGE_PREFIX + profile.getUuid() + "." + ext;

        deleteFromS3IfOurs(profile.getProfileImageUrl());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return buildPublicUrl(key);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MoplException(ProfileErrorCode.PROFILE_IMAGE_REQUIRED);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(contentType))) {
            throw new MoplException(ProfileErrorCode.PROFILE_IMAGE_INVALID_FORMAT);
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new MoplException(ProfileErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }
    }

    private void deleteFromS3IfOurs(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return;
        }
        String base = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (!profileImageUrl.startsWith(base)) {
            return;
        }
        String key = profileImageUrl.substring(base.length());
        if (!key.startsWith(PROFILE_IMAGE_PREFIX)) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String buildPublicUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}

