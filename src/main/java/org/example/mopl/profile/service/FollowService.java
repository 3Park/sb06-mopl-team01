package org.example.mopl.profile.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.event.message.UserFollowCreatedKafkaEvent;
import org.example.mopl.profile.dto.FollowDto;
import org.example.mopl.profile.dto.FollowRequest;
import org.example.mopl.profile.dto.FollowedByMeResponse;
import org.example.mopl.profile.dto.FollowerCountResponse;
import org.example.mopl.profile.entity.Follow;
import org.example.mopl.profile.exception.FollowSelfForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.repository.FollowRepository;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FollowDto create(UUID currentUserUuid, FollowRequest request) {
        if (currentUserUuid == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        UUID followeeUuid = request.getFolloweeId();
        if (currentUserUuid.equals(followeeUuid)) {
            throw new FollowSelfForbiddenException();
        }
        User follower = userRepository.findByUuid(currentUserUuid)
                .orElseThrow(() -> new ProfileNotFoundException(currentUserUuid));
        User followee = userRepository.findByUuid(followeeUuid)
                .orElseThrow(() -> new ProfileNotFoundException(followeeUuid));

        Long followerId = follower.getId();
        Long followeeId = followee.getId();
        return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .map(existing -> FollowDto.builder()
                        .id(existing.getUuid())
                        .followeeId(followee.getUuid())
                        .followerId(follower.getUuid())
                        .build())
                .orElseGet(() -> {
                    Follow follow = followRepository.save(Follow.of(follower, followee));
                    eventPublisher.publishEvent(
                            UserFollowCreatedKafkaEvent.of(
                                    followee.getUuid(),
                                    follower.getProfile() != null ? follower.getProfile().getName() : ""
                            )
                    );
                    return FollowDto.builder()
                            .id(follow.getUuid())
                            .followeeId(followee.getUuid())
                            .followerId(follower.getUuid())
                            .build();
                });
    }

    @Transactional
    public void deleteByUuid(UUID followUuid, UUID currentUserUuid) {
        if (currentUserUuid == null) {
            throw new org.example.mopl.profile.exception.ProfileUnauthorizedException();
        }
        User currentUser = userRepository.findByUuid(currentUserUuid)
                .orElseThrow(() -> new ProfileNotFoundException(currentUserUuid));
        followRepository.findByUuid(followUuid).ifPresent(follow -> {
            if (!follow.getFollower().getId().equals(currentUser.getId())) {
                throw new org.example.mopl.profile.exception.ProfileForbiddenException();
            }
            followRepository.delete(follow);
        });
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
}
