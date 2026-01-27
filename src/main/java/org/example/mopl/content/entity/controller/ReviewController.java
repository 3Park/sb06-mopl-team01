package org.example.mopl.content.entity.controller;

import jakarta.validation.Valid;
import org.example.mopl.content.entity.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.entity.dto.response.CursorResponseReviewDto;
import org.example.mopl.content.entity.dto.response.ReviewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/reviews")
public class ReviewController {

  // 리뷰 목록 조회 (커서 기반 페이지네이션)
  @GetMapping
  public ResponseEntity<CursorResponseReviewDto> getAllReviewsByCursor(
      @Valid @ModelAttribute CursorRequestReviewDto request
  ) {
    return ResponseEntity.ok().build();
  }

  // 리뷰 생성
  @PostMapping
  public ResponseEntity<ReviewDto> createReview() {
    return ResponseEntity.ok().build();
  }

  // 리뷰 수정
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReviewById(
      @PathVariable String reviewId
  ) {
    return ResponseEntity.ok().build();
  }

  // 리뷰 삭제
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReviewById(
      @PathVariable String reviewId
  ) {
    return ResponseEntity.ok().build();
  }

}
