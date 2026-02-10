package org.example.mopl.profile.service;

import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.dto.WatchingContentDto;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.entity.WatchingSession;
import org.example.mopl.profile.exception.ProfileForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.exception.ProfileUnauthorizedException;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.profile.repository.WatchingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileImageUploadService profileImageUploadService;
    private final WatchingSessionRepository watchingSessionRepository;

    public ProfileService(ProfileRepository profileRepository, ProfileImageUploadService profileImageUploadService, WatchingSessionRepository watchingSessionRepository) {
        this.profileRepository = profileRepository;
        this.profileImageUploadService = profileImageUploadService;
        this.watchingSessionRepository = watchingSessionRepository;
    }

    @Transactional(readOnly = true)
    public ProfileDto getByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        return toDto(profile, userId);
    }

    @Transactional
    public ProfileDto update(Long userId, ProfileUpdateRequest request, Long currentUserId) {
        if (currentUserId == null) {
            throw new ProfileUnauthorizedException();
        }
        if (!currentUserId.equals(userId)) {
            throw new ProfileForbiddenException();
        }
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        profile.update(request.getName(), request.getProfileImageUrl());
        return toDto(profile, userId);
    }

    @Transactional
    public ProfileDto uploadProfileImage(Long userId, MultipartFile file, Long currentUserId) throws IOException {
        if (currentUserId == null) {
            throw new ProfileUnauthorizedException();
        }
        if (!currentUserId.equals(userId)) {
            throw new ProfileForbiddenException();
        }
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        String imageUrl = profileImageUploadService.upload(profile, file);
        profile.updateProfileImageUrl(imageUrl);
        return toDto(profile, userId);
    }

    @Transactional(readOnly = true)
    public List<WatchingContentDto> getWatchingContents(Long userId) {
        List<WatchingSession> sessions = watchingSessionRepository.findActiveByWatcherId(userId);
        return sessions.stream()
                .map(this::toWatchingContentDto)
                .collect(Collectors.toList());
    }

    private WatchingContentDto toWatchingContentDto(WatchingSession session) {
        var c = session.getContent();
        return WatchingContentDto.builder()
                .sessionUuid(session.getUuid())
                .contentId(c.getId())
                .contentUuid(c.getUuid())
                .contentTitle(c.getTitle())
                .contentDescription(c.getDescription())
                .contentThumbnailUrl(c.getThumbnailUrl())
                .contentType(c.getContentType() != null ? c.getContentType().getValue() : null)
                .lastPosition(session.getLastPosition())
                .duration(session.getDuration())
                .status(session.getStatus().name())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private ProfileDto toDto(Profile profile, Long userId) {
        return ProfileDto.builder()
                .id(profile.getId())
                .uuid(profile.getUuid())
                .userId(userId)
                .name(profile.getName())
                .profileImageUrl(profile.getProfileImageUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}

