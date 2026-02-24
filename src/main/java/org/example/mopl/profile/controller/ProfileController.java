package org.example.mopl.profile.controller;

import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 사용자 상세 조회 (명세: GET /api/users/{userId}) */
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getByUserUuid(
            @PathVariable UUID userId,
            @AuthenticationPrincipal(expression = "userDto?.id") UUID currentUserUuid
    ) {
        ProfileDto dto = profileService.getByUserUuid(userId, currentUserUuid);
        return ResponseEntity.ok(dto);
    }

    /** 프로필 변경 (명세: PATCH /api/users/{userId}). multipart: request(JSON) + image(선택) */
    @PatchMapping("/{userId}")
    public ResponseEntity<ProfileDto> update(
            @PathVariable UUID userId,
            @RequestPart("request") @Valid ProfileUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID currentUserUuid = userDetails != null ? userDetails.getUserDto().getId() : null;
        ProfileDto dto = profileService.update(userId, request, image, currentUserUuid);
        return ResponseEntity.ok(dto);
    }
}
