package org.example.mopl.contentevaluation.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.request.PlaylistCreateRequest;
import org.example.mopl.contentevaluation.dto.request.PlaylistUpdateRequest;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.service.PlaylistCommandService;
import org.example.mopl.contentevaluation.service.PlaylistContentCommandService;
import org.example.mopl.contentevaluation.service.PlaylistQueryService;
import org.example.mopl.contentevaluation.service.SubscribeCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

  private final PlaylistQueryService playlistQueryService;
  private final PlaylistCommandService playlistCommandService;
  private final PlaylistContentCommandService playlistContentCommandService;
  private final SubscribeCommandService subscribeCommandService;

  // 플레이리스트 단건 조회
  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> getPlaylistById(@PathVariable UUID playlistId) {
    return ResponseEntity.ok(playlistQueryService.getPlaylistDtoByUuid(playlistId));
  }

  // 플레이리스트 목록 조회 (커서 페이지네이션)
  @GetMapping
  public ResponseEntity<CursorResponsePlaylistDto> getPlaylistsByCursor(
      @Valid @ModelAttribute CursorRequestPlaylistDto request
  ) {
    return ResponseEntity.ok(playlistQueryService.getPlaylistListByCursor(request));
  }

  // 플레이리스트 생성
  @PostMapping
  public ResponseEntity<PlaylistDto> createPlaylist(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody PlaylistCreateRequest request
  ) {
    return ResponseEntity.ok(playlistCommandService.createPlaylist(userDetails.getUserDto().getEmail(), request));
  }

  // 플레이리스트 구독
  @PostMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> subscribePlaylist(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId
  ) {

    subscribeCommandService.subscribePlaylist(userDetails.getUserDto().getEmail(), playlistId);

    return ResponseEntity.ok().build();

  }

  // 플레이리스트에 콘텐츠 추가
  @PostMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> addContentToPlaylist(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId
  ) {

    playlistContentCommandService.addContentToPlaylist(userDetails.getUserDto().getEmail(), playlistId, contentId);

    return ResponseEntity.ok().build();
  }

  // 플레이리스트 수정
  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> updatePlaylistById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId,
      @Valid @RequestBody PlaylistUpdateRequest request
  ) {
    return ResponseEntity.ok(playlistCommandService.updatePlaylist(
        userDetails.getUserDto().getEmail(),
        playlistId,
        request
    ));
  }

  // 플레이리스트 구독 취소
  @DeleteMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> unsubscribePlaylist(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId
  ) {

    subscribeCommandService.unsubscribePlaylist(userDetails.getUserDto().getEmail(), playlistId);

    return ResponseEntity.ok().build();
  }

  // 플레이리스트에서 콘텐츠 삭제
  @DeleteMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> removeContentFromPlaylist(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId
  ) {

    playlistContentCommandService.removeContentFromPlaylist(
        userDetails.getUserDto().getEmail(),
        playlistId,
        contentId
    );

    return ResponseEntity.ok().build();
  }

  // 플레이리스트 삭제
  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> deletePlaylistById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID playlistId
  ) {

    playlistCommandService.deletePlaylistByUuid(
        userDetails.getUserDto().getEmail(),
        playlistId
    );

    return ResponseEntity.ok().build();
  }


}
