package org.example.mopl.profile.controller;

import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.profile.dto.SubscribedPlaylistCursorResponse;
import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.dto.UserSummary;
import org.example.mopl.profile.dto.WatchingContentDto;
import org.example.mopl.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

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

    @PostMapping(value = "/users/{userId}/image", consumes = "multipart/form-data")
    public ResponseEntity<ProfileDto> uploadProfileImage(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) throws IOException {
        ProfileDto dto = profileService.uploadProfileImage(userId, file, currentUserId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/users/{userId}/watching")
    public ResponseEntity<List<WatchingContentDto>> getWatchingContents(@PathVariable Long userId) {
        List<WatchingContentDto> list = profileService.getWatchingContents(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/users/{userId}/playlists")
    public ResponseEntity<CursorResponsePlaylistDto> getOwnedPlaylists(
            @PathVariable Long userId,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        CursorResponsePlaylistDto result = profileService.getOwnedPlaylists(userId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{userId}/subscribed-playlists")
    public ResponseEntity<SubscribedPlaylistCursorResponse> getSubscribedPlaylists(
            @PathVariable Long userId,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        SubscribedPlaylistCursorResponse result = profileService.getSubscribedPlaylists(userId, request);
        return ResponseEntity.ok(result);
    }

    /** 단일 사용자 프로필 요약 (알림/DM 연계용) */
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<UserSummary> getUserSummary(@PathVariable Long userId) {
        UserSummary summary = profileService.getUserSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /** 여러 사용자 프로필 요약 일괄 조회 (알림/DM 연계용). userIds=1&userIds=2&userIds=3 */
    @GetMapping("/summaries")
    public ResponseEntity<List<UserSummary>> getUserSummaries(@RequestParam List<Long> userIds) {
        List<UserSummary> list = profileService.getUserSummaries(userIds);
        return ResponseEntity.ok(list);
    }
}

