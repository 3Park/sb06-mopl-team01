package org.example.mopl.profile.controller;

import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.profile.dto.SubscribedPlaylistCursorResponse;
import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.dto.WatchingContentDto;
import org.example.mopl.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 사용자 상세 조회 = 프로필 조회 (명세: GET /api/users/{userId}) */
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getByUserUuid(@PathVariable UUID userId) {
        ProfileDto dto = profileService.getByUserUuid(userId);
        return ResponseEntity.ok(dto);
    }

    /* 프로필 변경 */
    @PatchMapping("/{userId}")
    public ResponseEntity<ProfileDto> update(
            @PathVariable UUID userId,
            @RequestBody @Valid ProfileUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) {
        ProfileDto dto = profileService.update(userId, request, currentUserUuid);
        return ResponseEntity.ok(dto);
    }

    /* 프로필 이미지 업로드 */
    @PostMapping(value = "/{userId}/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<ProfileDto> uploadProfileImage(
            @PathVariable UUID userId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) throws IOException {
        ProfileDto dto = profileService.uploadProfileImage(userId, file, currentUserUuid);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/watching")
    public ResponseEntity<List<WatchingContentDto>> getWatchingContents(@PathVariable UUID userId) {
        List<WatchingContentDto> list = profileService.getWatchingContents(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userId}/playlists")
    public ResponseEntity<CursorResponsePlaylistDto> getOwnedPlaylists(
            @PathVariable UUID userId,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        CursorResponsePlaylistDto result = profileService.getOwnedPlaylists(userId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/subscribed-playlists")
    public ResponseEntity<SubscribedPlaylistCursorResponse> getSubscribedPlaylists(
            @PathVariable UUID userId,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        SubscribedPlaylistCursorResponse result = profileService.getSubscribedPlaylists(userId, request);
        return ResponseEntity.ok(result);
    }
}
