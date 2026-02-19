package org.example.mopl.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.example.mopl.profile.dto.FollowCreateRequest;
import org.example.mopl.profile.dto.FollowedByMeResponse;
import org.example.mopl.profile.dto.FollowerCountResponse;
import org.example.mopl.profile.exception.FollowSelfForbiddenException;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.profile.repository.ProfileRepository;
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
    @DisplayName("create: currentUserId가 null이면 ProfileUnauthorizedException")
    void create_throwsWhenUnauthorized() {
        FollowCreateRequest request = new FollowCreateRequest();
        ReflectionTestUtils.setField(request, "followeeId", 2L);

        assertThatThrownBy(() -> followService.create(null, request))
                .isInstanceOf(org.example.mopl.profile.exception.ProfileUnauthorizedException.class);
    }

    @Test
    @DisplayName("create: 자기 자신을 팔로우하면 FollowSelfForbiddenException")
    void create_throwsWhenSelfFollow() {
        Long userId = 1L;
        FollowCreateRequest request = new FollowCreateRequest();
        ReflectionTestUtils.setField(request, "followeeId", userId);

        assertThatThrownBy(() -> followService.create(userId, request))
                .isInstanceOf(FollowSelfForbiddenException.class);
    }

    @Test
    @DisplayName("isFollowedByMe: followerId가 null이면 followed false")
    void isFollowedByMe_returnsFalseWhenFollowerIdNull() {
        FollowedByMeResponse result = followService.isFollowedByMe(null, 1L);

        assertThat(result.isFollowed()).isFalse();
    }

    @Test
    @DisplayName("isFollowedByMe: 팔로우 중이면 true")
    void isFollowedByMe_returnsTrueWhenFollowing() {
        Long followerId = 1L;
        Long followeeId = 2L;
        given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(true);

        FollowedByMeResponse result = followService.isFollowedByMe(followerId, followeeId);

        assertThat(result.isFollowed()).isTrue();
    }

    @Test
    @DisplayName("getFollowerCount: followeeId의 팔로워 수를 반환")
    void getFollowerCount_returnsCount() {
        Long followeeId = 1L;
        given(followRepository.countByFolloweeId(followeeId)).willReturn(10L);

        FollowerCountResponse result = followService.getFollowerCount(followeeId);

        assertThat(result.getCount()).isEqualTo(10L);
        verify(followRepository).countByFolloweeId(followeeId);
    }
}
