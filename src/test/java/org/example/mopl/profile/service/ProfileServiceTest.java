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

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private Profile createMockProfile(UUID userUuid, String name, String profileImageUrl) {
        org.example.mopl.user.entity.User user = mock(org.example.mopl.user.entity.User.class);
        when(user.getUuid()).thenReturn(userUuid);
        Profile profile = mock(Profile.class);
        lenient().when(profile.getId()).thenReturn(100L);
        lenient().when(profile.getUuid()).thenReturn(UUID.randomUUID());
        when(profile.getUser()).thenReturn(user);
        lenient().when(profile.getName()).thenReturn(name);
        lenient().when(profile.getProfileImageUrl()).thenReturn(profileImageUrl);
        lenient().when(profile.getCreatedAt()).thenReturn(LocalDateTime.now());
        lenient().when(profile.getUpdatedAt()).thenReturn(LocalDateTime.now());
        return profile;
    }

    @Test
    @DisplayName("getByUserUuid: 프로필이 있으면 ProfileDto, id=userUuid")
    void getByUserUuid_returnsDto() {
        UUID userUuid = UUID.randomUUID();
        Profile profile = createMockProfile(userUuid, "테스트유저", "https://example.com/img.jpg");
        given(profileRepository.findWithUserByUserUuid(userUuid)).willReturn(Optional.of(profile));

        ProfileDto result = profileService.getByUserUuid(userUuid, userUuid);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userUuid);
        assertThat(result.getUserUuid()).isEqualTo(userUuid);
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/img.jpg");
        verify(profileRepository).findWithUserByUserUuid(userUuid);
    }

    @Test
    @DisplayName("getByUserUuid: 프로필이 없으면 ProfileNotFoundException")
    void getByUserUuid_throwsWhenNotFound() {
        UUID userUuid = UUID.randomUUID();
        given(profileRepository.findWithUserByUserUuid(userUuid)).willReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByUserUuid(userUuid, null))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("update: currentUserUuid가 null이면 ProfileUnauthorizedException")
    void update_throwsWhenUnauthorized() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("새이름");

        assertThatThrownBy(() -> profileService.update(UUID.randomUUID(), request, null, null))
                .isInstanceOf(ProfileUnauthorizedException.class);
    }

    @Test
    @DisplayName("update: 다른 유저 프로필 수정 시도 시 ProfileForbiddenException")
    void update_throwsWhenForbidden() {
        UUID userUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();
        Profile profile = createMockProfile(userUuid, "테스트유저", null);
        given(profileRepository.findWithUserByUserUuid(userUuid)).willReturn(Optional.of(profile));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("새이름");

        assertThatThrownBy(() -> profileService.update(userUuid, request, null, otherUserUuid))
                .isInstanceOf(ProfileForbiddenException.class);
    }
}
