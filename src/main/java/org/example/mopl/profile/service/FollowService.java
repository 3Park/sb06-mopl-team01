package org.example.mopl.profile.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.event.message.UserFollowCreatedKafkaEvent;
import org.example.mopl.profile.dto.FollowCreateRequest;
import org.example.mopl.profile.dto.FollowResponse;
import org.example.mopl.profile.dto.FollowUserDto;
import org.example.mopl.profile.dto.FollowedByMeResponse;
import org.example.mopl.profile.dto.FollowerCountResponse;
import org.example.mopl.profile.entity.Follow;
import org.example.mopl.profile.exception.FollowAlreadyExistsException;
import org.example.mopl.profile.exception.FollowNotFoundException;
import org.example.mopl.profile.exception.FollowSelfForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FollowResponse create(Long currentUserId, FollowCreateRequest request) {
        if (currentUserId == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        if (currentUserId.equals(request.getFolloweeId())) {
            throw new FollowSelfForbiddenException();
        }
        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ProfileNotFoundException(currentUserId));
        User followee = userRepository.findById(request.getFolloweeId())
                .orElseThrow(() -> new ProfileNotFoundException(request.getFolloweeId()));

        if (followRepository.existsByFollowerIdAndFolloweeId(currentUserId, request.getFolloweeId())) {
            throw new FollowAlreadyExistsException(currentUserId, request.getFolloweeId());
        }

        Follow follow = followRepository.save(Follow.of(follower, followee));

        eventPublisher.publishEvent(
                UserFollowCreatedKafkaEvent.of(
                        followee.getUuid(),
                        follower.getProfile() != null ? follower.getProfile().getName() : ""
                )
        );

        return FollowResponse.builder()
                .followUuid(follow.getUuid())
                .build();
    }

    @Transactional
    public void deleteByUuid(UUID followUuid, Long currentUserId) {
        if (currentUserId == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        Follow follow = followRepository.findByUuid(followUuid)
                .orElseThrow(() -> new FollowNotFoundException(followUuid));
        if (!follow.getFollower().getId().equals(currentUserId)) {
            throw new org.example.mopl.profile.exception.ProfileForbiddenException();
        }
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public FollowedByMeResponse isFollowedByMe(Long followerId, Long followeeId) {
        if (followerId == null) {
            return FollowedByMeResponse.builder().followed(false).build();
        }
        boolean followed = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
        return FollowedByMeResponse.builder().followed(followed).build();
    }

    @Transactional(readOnly = true)
    public FollowerCountResponse getFollowerCount(Long followeeId) {
        long count = followRepository.countByFolloweeId(followeeId);
        return FollowerCountResponse.builder().count(count).build();
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowingList(Long userId) {
        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        List<Follow> follows = followRepository.findAllByFollowerIdWithFollowee(userId);
        return follows.stream()
                .map(f -> toFollowUserDto(f.getFollowee()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowerList(Long userId) {
        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        List<Follow> follows = followRepository.findAllByFolloweeIdWithFollower(userId);
        return follows.stream()
                .map(f -> toFollowUserDto(f.getFollower()))
                .toList();
    }

    private FollowUserDto toFollowUserDto(User user) {
        return FollowUserDto.builder()
                .userId(user.getId())
                .userUuid(user.getUuid())
                .name(user.getProfile() != null ? user.getProfile().getName() : null)
                .profileImageUrl(user.getProfile() != null ? user.getProfile().getProfileImageUrl() : null)
                .build();
    }
}
