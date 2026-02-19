package org.example.mopl.content.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.dto.response.CursorResponseContentDto;
import org.example.mopl.content.service.ContentCommandService;
import org.example.mopl.content.service.ContentQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

  private final ContentQueryService contentQueryService;
  private final ContentCommandService contentCommandService;

  // 콘텐츠 단건 조회
  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> getContentById(@PathVariable UUID contentId) {
    return ResponseEntity.ok(contentQueryService.getContentByUuid(contentId));

  }

  // 콘텐츠 목록 조회 (커서 기반 페이지네이션)
  @GetMapping
  public  ResponseEntity<CursorResponseContentDto> getAllContentsByCursor(@Valid @ModelAttribute
      CursorRequestContentDto request) {
    return ResponseEntity.ok(contentQueryService.getContentsByCursor(request));
  }

  // 콘텐츠 생성
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ContentDto> createContent(
      @Valid @RequestPart("request") ContentCreateRequest request,
      @RequestPart("thumbnail") MultipartFile thumbnail
  ) {
    return ResponseEntity.ok(contentCommandService.createContent(request, thumbnail));
  }

  // 콘텐츠 수정
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{contentId}")
  public ResponseEntity<ContentDto> updateContentById(
      @PathVariable UUID contentId,
      @Valid @RequestPart("request") ContentUpdateRequest request,
      @RequestPart("thumbnail") MultipartFile thumbnail
  ) {
    return ResponseEntity.ok(contentCommandService.updateContent(contentId, request, thumbnail));
  }

  // 콘텐츠 삭제
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{contentId}")
  public ResponseEntity<Void> deleteContentById(
      @PathVariable UUID contentId
  ) {

    contentCommandService.deleteContentByUuid(contentId);

    return ResponseEntity.ok().build();
  }


}
