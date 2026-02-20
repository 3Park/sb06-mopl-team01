package org.example.mopl.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.example.mopl.profile.dto.FollowCreateRequest;
import org.example.mopl.profile.dto.FollowedByMeResponse;
import org.example.mopl.profile.dto.FollowerCountResponse;
import org.example.mopl.profile.exception.FollowSelfForbiddenException;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private org.example.mopl.user.repository.UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowService followService;

    @Test
    @DisplayName("create: currentUserUuid가 null이면 ProfileUnauthorizedException")
    void create_throwsWhenUnauthorized() {
        FollowCreateRequest request = new FollowCreateRequest();
        ReflectionTestUtils.setField(request, "followeeUuid", UUID.randomUUID());

        assertThatThrownBy(() -> followService.create(null, request))
                .isInstanceOf(org.example.mopl.profile.exception.ProfileUnauthorizedException.class);
    }

    @Test
    @DisplayName("create: 자기 자신을 팔로우하면 FollowSelfForbiddenException")
    void create_throwsWhenSelfFollow() {
        UUID userUuid = UUID.randomUUID();
        FollowCreateRequest request = new FollowCreateRequest();
        ReflectionTestUtils.setField(request, "followeeUuid", userUuid);

        assertThatThrownBy(() -> followService.create(userUuid, request))
                .isInstanceOf(FollowSelfForbiddenException.class);
    }

    @Test
    @DisplayName("isFollowedByMe: followerUuid가 null이면 followed false")
    void isFollowedByMe_returnsFalseWhenFollowerUuidNull() {
        FollowedByMeResponse result = followService.isFollowedByMe(null, UUID.randomUUID());

        assertThat(result.isFollowed()).isFalse();
    }

    @Test
    @DisplayName("isFollowedByMe: 팔로우 중이면 true")
    void isFollowedByMe_returnsTrueWhenFollowing() {
        UUID followerUuid = UUID.randomUUID();
        UUID followeeUuid = UUID.randomUUID();
        User follower = mock(User.class);
        User followee = mock(User.class);
        when(follower.getId()).thenReturn(1L);
        when(followee.getId()).thenReturn(2L);
        given(userRepository.findByUuid(followerUuid)).willReturn(Optional.of(follower));
        given(userRepository.findByUuid(followeeUuid)).willReturn(Optional.of(followee));
        given(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).willReturn(true);

        FollowedByMeResponse result = followService.isFollowedByMe(followerUuid, followeeUuid);

        assertThat(result.isFollowed()).isTrue();
    }

    @Test
    @DisplayName("getFollowerCount: followeeUuid의 팔로워 수를 반환")
    void getFollowerCount_returnsCount() {
        UUID followeeUuid = UUID.randomUUID();
        User followee = mock(User.class);
        when(followee.getId()).thenReturn(1L);
        given(userRepository.findByUuid(followeeUuid)).willReturn(Optional.of(followee));
        given(followRepository.countByFolloweeId(1L)).willReturn(10L);

        FollowerCountResponse result = followService.getFollowerCount(followeeUuid);

        assertThat(result.getCount()).isEqualTo(10L);
    }
}
