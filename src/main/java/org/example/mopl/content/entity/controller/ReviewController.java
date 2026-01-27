package org.example.mopl.content.entity.controller;

import jakarta.validation.Valid;
import org.example.mopl.content.entity.dto.response.CursorResponseReviewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/reviews")
public class ReviewController {

  @GetMapping
  public ResponseEntity<CursorResponseReviewDto> getAllReviewsByCursor(
  ) {
    return ResponseEntity.ok().build();
  }

}
