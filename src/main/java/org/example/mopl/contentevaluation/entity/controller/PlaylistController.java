package org.example.mopl.contentevaluation.entity.controller;

import jakarta.validation.Valid;
import org.example.mopl.content.entity.dto.request.CursorRequestReviewDto;
import org.example.mopl.contentevaluation.entity.dto.request.PlaylistCreateRequest;
import org.example.mopl.contentevaluation.entity.dto.request.PlaylistUpdateRequest;
import org.example.mopl.contentevaluation.entity.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.contentevaluation.entity.dto.response.PlaylistDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/playlists")
public class PlaylistController {

  // 플레이리스트 단건 조회
  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> getPlaylistById(@PathVariable String playlistId) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 목록 조회 (커서 페이지네이션)
  @GetMapping
  public ResponseEntity<CursorResponsePlaylistDto> getPlaylistsByCursor(
      @Valid @ModelAttribute CursorRequestReviewDto request
  ) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 생성
  @PostMapping
  public ResponseEntity<PlaylistDto> createPlaylist(
      @Valid @ModelAttribute PlaylistCreateRequest request
  ) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 구독
  @PostMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> subscribePlaylist(@PathVariable String playlistId) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트에 콘텐츠 추가
  @PostMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> addContentToPlaylist(
      @PathVariable String playlistId,
      @PathVariable String contentId
  ) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 수정
  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> updatePlaylistById(
      @PathVariable String playlistId,
      @Valid @ModelAttribute PlaylistUpdateRequest request
  ) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 구독 취소
  @DeleteMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> unsubscribePlaylist(@PathVariable String playlistId) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트에서 콘텐츠 삭제
  @DeleteMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> removeContentFromPlaylist(
      @PathVariable String playlistId,
      @PathVariable String contentId
  ) {
    return ResponseEntity.ok().build();
  }

  // 플레이리스트 삭제
  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> deletePlaylistById(@PathVariable String playlistId) {
    return ResponseEntity.ok().build();
  }


}
