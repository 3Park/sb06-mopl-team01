package org.example.mopl.profile.service;

import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.common.exception.MoplException;
import org.example.mopl.profile.exception.ProfileErrorCode;
import org.example.mopl.profile.exception.ProfileForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.exception.ProfileUnauthorizedException;
import org.example.mopl.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileImageUploadService profileImageUploadService;

    public ProfileService(ProfileRepository profileRepository,
                          ProfileImageUploadService profileImageUploadService) {
        this.profileRepository = profileRepository;
        this.profileImageUploadService = profileImageUploadService;
    }

    @Transactional(readOnly = true)
    public ProfileDto getByUserUuid(UUID userUuid, UUID currentUserUuid) {
        Profile profile = profileRepository.findWithUserAndRolesByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        return toDto(profile);
    }

    @Transactional
    public ProfileDto update(UUID userUuid, ProfileUpdateRequest request,
                             MultipartFile image, UUID currentUserUuid) {
        if (currentUserUuid == null) {
            throw new ProfileUnauthorizedException();
        }
        Profile profile = profileRepository.findWithUserAndRolesByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        if (!profile.getUser().getUuid().equals(currentUserUuid)) {
            throw new ProfileForbiddenException();
        }
        String profileImageUrl;
        if (image != null && !image.isEmpty()) {
            try {
                profileImageUrl = profileImageUploadService.upload(profile, image);
            } catch (IOException e) {
                throw new MoplException(ProfileErrorCode.PROFILE_IMAGE_UPLOAD_FAILED, e);
            }
        } else {
            String requested = request.getProfileImageUrl();
            profileImageUrl = (requested != null && !requested.isBlank())
                    ? requested
                    : profile.getProfileImageUrl();
        }
        profile.update(request.getName(), profileImageUrl);
        return toDto(profile);
    }

    private ProfileDto toDto(Profile profile) {
        var user = profile.getUser();
        String role = user.getUserRoles().stream()
                .findFirst()
                .map(ur -> ur.getRole().getName().name())
                .orElse(null);
        return ProfileDto.builder()
                .id(user.getUuid())
                .email(user.getEmail())
                .name(profile.getName())
                .profileImageUrl(profile.getProfileImageUrl())
                .role(role)
                .locked(user.getLocked() != null ? user.getLocked() : false)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}


