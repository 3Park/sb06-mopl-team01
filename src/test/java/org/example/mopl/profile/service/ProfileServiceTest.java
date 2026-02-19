package org.example.mopl.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.exception.ProfileForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.exception.ProfileUnauthorizedException;
import org.example.mopl.profile.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileImageUploadService profileImageUploadService;
    @Mock
    private org.example.mopl.profile.repository.WatchingSessionRepository watchingSessionRepository;
    @Mock
    private org.example.mopl.contentevaluation.service.PlaylistQueryService playlistQueryService;
    @Mock
    private org.example.mopl.profile.repository.SubscribedPlaylistQueryRepository subscribedPlaylistQueryRepository;

    @InjectMocks
    private ProfileService profileService;

    private Profile createMockProfile(Long userId, String name, String profileImageUrl) {
        Profile profile = mock(Profile.class);
        when(profile.getId()).thenReturn(100L);
        when(profile.getUuid()).thenReturn(UUID.randomUUID());
        when(profile.getName()).thenReturn(name);
        when(profile.getProfileImageUrl()).thenReturn(profileImageUrl);
        when(profile.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(profile.getUpdatedAt()).thenReturn(LocalDateTime.now());
        return profile;
    }

    @Test
    @DisplayName("getByUserId: 프로필이 있으면 ProfileDto")
    void getByUserId_returnsDto() {
        Long userId = 1L;
        Profile profile = createMockProfile(userId, "테스트유저", "https://example.com/img.jpg");
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        ProfileDto result = profileService.getByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/img.jpg");
        verify(profileRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getByUserId: 프로필이 없으면 ProfileNotFoundException")
    void getByUserId_throwsWhenNotFound() {
        Long userId = 999L;
        given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByUserId(userId))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("update: currentUserId가 null이면 ProfileUnauthorizedException")
    void update_throwsWhenUnauthorized() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("새이름");

        assertThatThrownBy(() -> profileService.update(1L, request, null))
                .isInstanceOf(ProfileUnauthorizedException.class);
    }

    @Test
    @DisplayName("update: 다른 유저 프로필 수정 시도 시 ProfileForbiddenException")
    void update_throwsWhenForbidden() {
        Long userId = 1L;
        Long otherUserId = 2L;
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("새이름");

        assertThatThrownBy(() -> profileService.update(userId, request, otherUserId))
                .isInstanceOf(ProfileForbiddenException.class);
    }
}
