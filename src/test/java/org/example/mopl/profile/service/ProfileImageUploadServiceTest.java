package org.example.mopl.profile.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.example.mopl.common.exception.MoplException;
import org.example.mopl.profile.config.ProfileImageProperties;
import org.example.mopl.profile.entity.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
class ProfileImageUploadServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ProfileImageProperties imageProperties;

    @InjectMocks
    private ProfileImageUploadService profileImageUploadService;

    @BeforeEach
    void setUpImageProperties() {
        lenient().when(imageProperties.getMaxSize()).thenReturn(5 * 1024 * 1024L);
        lenient().when(imageProperties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));
        lenient().when(imageProperties.getKeyPrefix()).thenReturn("profile/");
    }

    @Test
    @DisplayName("upload: 파일이 null이면 PROFILE_IMAGE_REQUIRED")
    void upload_throwsWhenFileNull() throws IOException {
        Profile profile = mockProfile(UUID.randomUUID(), null);

        assertThatThrownBy(() -> profileImageUploadService.upload(profile, null))
                .isInstanceOf(MoplException.class)
                .hasMessageContaining("프로필 이미지");
    }

    @Test
    @DisplayName("upload: 허용되지 않은 Content-Type이면 PROFILE_IMAGE_INVALID_FORMAT")
    void upload_throwsWhenInvalidContentType() throws IOException {
        ReflectionTestUtils.setField(profileImageUploadService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(profileImageUploadService, "region", "ap-northeast-2");
        Profile profile = mockProfile(UUID.randomUUID(), null);
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getContentType()).willReturn("image/gif");

        assertThatThrownBy(() -> profileImageUploadService.upload(profile, file))
                .isInstanceOf(MoplException.class)
                .hasMessageContaining("허용되지 않는 이미지 형식");
    }

    @Test
    @DisplayName("upload: 파일 크기가 5MB 초과하면 PROFILE_IMAGE_TOO_LARGE")
    void upload_throwsWhenTooLarge() throws IOException {
        ReflectionTestUtils.setField(profileImageUploadService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(profileImageUploadService, "region", "ap-northeast-2");
        Profile profile = mockProfile(UUID.randomUUID(), null);
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getContentType()).willReturn("image/jpeg");
        given(file.getSize()).willReturn(6 * 1024 * 1024L); // 6MB

        assertThatThrownBy(() -> profileImageUploadService.upload(profile, file))
                .isInstanceOf(MoplException.class)
                .hasMessageContaining("용량");
    }

    private Profile mockProfile(UUID uuid, String profileImageUrl) {
        Profile profile = mock(Profile.class);
        lenient().when(profile.getUuid()).thenReturn(uuid);
        lenient().when(profile.getProfileImageUrl()).thenReturn(profileImageUrl);
        return profile;
    }
}
