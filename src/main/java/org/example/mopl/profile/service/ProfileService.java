package org.example.mopl.profile.service;

import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.exception.ProfileForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.exception.ProfileUnauthorizedException;
import org.example.mopl.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileImageUploadService profileImageUploadService;

    public ProfileService(ProfileRepository profileRepository, ProfileImageUploadService profileImageUploadService) {
        this.profileRepository = profileRepository;
        this.profileImageUploadService = profileImageUploadService;
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
