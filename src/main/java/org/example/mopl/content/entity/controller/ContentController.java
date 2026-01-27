package org.example.mopl.content.entity.controller;

import jakarta.validation.Valid;
import org.example.mopl.content.entity.dto.request.CursorRequestContentDto;
import org.example.mopl.content.entity.dto.response.ContentDto;
import org.example.mopl.content.entity.dto.response.CursorResponseContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/contents")
public class ContentController {

  // 콘텐츠 단건 조회
  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> getContentById() {
    return ResponseEntity.ok().build();
  }

  // 콘텐츠 목록 조회 (커서 기반 페이지네이션)
  @GetMapping
  public  ResponseEntity<CursorResponseContentDto> getAllContentsByCursor(@Valid @ModelAttribute
      CursorRequestContentDto request) {
    return ResponseEntity.ok().build();
  }

  // 콘텐츠 생성
  @PostMapping
  public ResponseEntity<ContentDto> createContent() {
    return ResponseEntity.ok().build();
  }

  // 콘텐츠 수정
  @PatchMapping("/{contentId}")
  public ResponseEntity<ContentDto> updateContentById(
      @PathVariable String contentId
  ) {
    return ResponseEntity.ok().build();
  }

  // 콘텐츠 삭제
  @DeleteMapping("/{contentId}")
  public ResponseEntity<Void> deleteContentById(
      @PathVariable String contentId
  ) {
    return ResponseEntity.ok().build();
  }


}
