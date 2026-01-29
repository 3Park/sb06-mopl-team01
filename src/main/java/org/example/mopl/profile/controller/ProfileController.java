package org.example.mopl.profile.controller;

import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // 프로필 기본 정보 조회 (userId 기준)
    @GetMapping("/users/{userId}")
    public ResponseEntity<ProfileDto> getByUserId(@PathVariable Long userId) {
        ProfileDto dto = profileService.getByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    // 프로필 수정 (본인만). 
    @PatchMapping("/users/{userId}")
    public ResponseEntity<ProfileDto> update(
            @PathVariable Long userId,
            @RequestBody @Valid ProfileUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        ProfileDto dto = profileService.update(userId, request, currentUserId);
        return ResponseEntity.ok(dto);
    }
}
