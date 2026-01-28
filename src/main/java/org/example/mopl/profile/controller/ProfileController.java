package org.example.mopl.profile.controller;

import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
