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
    public FollowResponse create(UUID currentUserUuid, FollowCreateRequest request) {
        if (currentUserUuid == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        UUID followeeUuid = request.getFolloweeUuid();
        if (currentUserUuid.equals(followeeUuid)) {
            throw new FollowSelfForbiddenException();
        }
        User follower = userRepository.findByUuid(currentUserUuid)
                .orElseThrow(() -> new ProfileNotFoundException(currentUserUuid));
        User followee = userRepository.findByUuid(followeeUuid)
                .orElseThrow(() -> new ProfileNotFoundException(followeeUuid));

        Long followerId = follower.getId();
        Long followeeId = followee.getId();
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new FollowAlreadyExistsException(currentUserUuid, followeeUuid);
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
    public void deleteByUuid(UUID followUuid, UUID currentUserUuid) {
        if (currentUserUuid == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        User currentUser = userRepository.findByUuid(currentUserUuid)
                .orElseThrow(() -> new ProfileNotFoundException(currentUserUuid));
        Follow follow = followRepository.findByUuid(followUuid)
                .orElseThrow(() -> new FollowNotFoundException(followUuid));
        if (!follow.getFollower().getId().equals(currentUser.getId())) {
            throw new org.example.mopl.profile.exception.ProfileForbiddenException();
        }
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public FollowedByMeResponse isFollowedByMe(UUID followerUuid, UUID followeeUuid) {
        if (followerUuid == null) {
            return FollowedByMeResponse.builder().followed(false).build();
        }
        User follower = userRepository.findByUuid(followerUuid).orElse(null);
        User followee = userRepository.findByUuid(followeeUuid).orElse(null);
        if (follower == null || followee == null) {
            return FollowedByMeResponse.builder().followed(false).build();
        }
        boolean followed = followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId());
        return FollowedByMeResponse.builder().followed(followed).build();
    }

    @Transactional(readOnly = true)
    public FollowerCountResponse getFollowerCount(UUID followeeUuid) {
        User followee = userRepository.findByUuid(followeeUuid)
                .orElseThrow(() -> new ProfileNotFoundException(followeeUuid));
        long count = followRepository.countByFolloweeId(followee.getId());
        return FollowerCountResponse.builder().count(count).build();
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowingList(UUID userUuid) {
        profileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        User user = userRepository.findByUuid(userUuid).orElseThrow(() -> new ProfileNotFoundException(userUuid));
        List<Follow> follows = followRepository.findAllByFollowerIdWithFollowee(user.getId());
        return follows.stream()
                .map(f -> toFollowUserDto(f.getFollowee()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowerList(UUID userUuid) {
        profileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        User user = userRepository.findByUuid(userUuid).orElseThrow(() -> new ProfileNotFoundException(userUuid));
        List<Follow> follows = followRepository.findAllByFolloweeIdWithFollower(user.getId());
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
