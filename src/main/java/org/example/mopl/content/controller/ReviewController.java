package org.example.mopl.content.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.content.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.dto.request.ReviewCreateRequest;
import org.example.mopl.content.dto.request.ReviewUpdateRequest;
import org.example.mopl.content.dto.response.CursorResponseReviewDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.service.ReviewCommandService;
import org.example.mopl.content.service.ReviewQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewQueryService reviewQueryService;
  private final ReviewCommandService reviewCommandService;

  // 리뷰 목록 조회 (커서 기반 페이지네이션)
  @GetMapping
  public ResponseEntity<CursorResponseReviewDto> getAllReviewsByCursor(
      @Valid @ModelAttribute CursorRequestReviewDto request
  ) {
    return ResponseEntity.ok(reviewQueryService.getReviewsByCursor(request));
  }

  // 리뷰 생성
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @ModelAttribute ReviewCreateRequest request
  ) {
    return ResponseEntity.ok(reviewCommandService.createReview(userDetails.getUserDto().getEmail(), request));
  }

  // 리뷰 수정
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReviewById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID reviewId,
      @Valid @ModelAttribute ReviewUpdateRequest request
  ) {
    return ResponseEntity.ok(reviewCommandService.updateReview(
        userDetails.getUserDto().getEmail(),
        reviewId,
        request
    ));
  }

  // 리뷰 삭제
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReviewById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID reviewId
  ) {

    reviewCommandService.deleteReviewByUuid(userDetails.getUserDto().getEmail(), reviewId);

    return ResponseEntity.ok().build();

  }

}
