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
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 프로필 기본 정보 조회 */
    @GetMapping("/{userUuid}/profile")
    public ResponseEntity<ProfileDto> getByUserUuid(@PathVariable UUID userUuid) {
        ProfileDto dto = profileService.getByUserUuid(userUuid);
        return ResponseEntity.ok(dto);
    }

    /** 프로필 수정 (본인만) */
    @PatchMapping("/{userUuid}/profile")
    public ResponseEntity<ProfileDto> update(
            @PathVariable UUID userUuid,
            @RequestBody @Valid ProfileUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) {
        ProfileDto dto = profileService.update(userUuid, request, currentUserUuid);
        return ResponseEntity.ok(dto);
    }

    /** 프로필 이미지 업로드 */
    @PostMapping(value = "/{userUuid}/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<ProfileDto> uploadProfileImage(
            @PathVariable UUID userUuid,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) throws IOException {
        ProfileDto dto = profileService.uploadProfileImage(userUuid, file, currentUserUuid);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userUuid}/watching")
    public ResponseEntity<List<WatchingContentDto>> getWatchingContents(@PathVariable UUID userUuid) {
        List<WatchingContentDto> list = profileService.getWatchingContents(userUuid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userUuid}/playlists")
    public ResponseEntity<CursorResponsePlaylistDto> getOwnedPlaylists(
            @PathVariable UUID userUuid,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        CursorResponsePlaylistDto result = profileService.getOwnedPlaylists(userUuid, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userUuid}/subscribed-playlists")
    public ResponseEntity<SubscribedPlaylistCursorResponse> getSubscribedPlaylists(
            @PathVariable UUID userUuid,
            @ModelAttribute @Valid CursorRequestPlaylistDto request
    ) {
        SubscribedPlaylistCursorResponse result = profileService.getSubscribedPlaylists(userUuid, request);
        return ResponseEntity.ok(result);
    }

    /** 단일 사용자 프로필 요약 (알림/DM 연계용) */
    @GetMapping("/{userUuid}/summary")
    public ResponseEntity<UserSummary> getUserSummary(@PathVariable UUID userUuid) {
        UserSummary summary = profileService.getUserSummary(userUuid);
        return ResponseEntity.ok(summary);
    }

    /** 여러 사용자 프로필 요약 일괄 조회 (알림/DM 연계용). userIds=uuid1&userIds=uuid2 */
    @GetMapping("/summaries")
    public ResponseEntity<List<UserSummary>> getUserSummaries(@RequestParam List<UUID> userIds) {
        List<UserSummary> list = profileService.getUserSummaries(userIds);
        return ResponseEntity.ok(list);
    }
}
