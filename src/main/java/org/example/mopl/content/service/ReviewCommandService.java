package org.example.mopl.content.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.ReviewCreateRequest;
import org.example.mopl.content.dto.request.ReviewUpdateRequest;
import org.example.mopl.content.dto.response.AuthorDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.Review;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchReviewException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ReviewCommandRepository;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {

  private final ContentQueryRepository contentQueryRepository;
  private final ReviewCommandRepository reviewCommandRepository;
  private final ReviewQueryRepository reviewQueryRepository;

  // Todo : UserRepository와 시큐리티 사용해 User id 가져오기
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {

    Content content = contentQueryRepository.findByUuid(request.contentId())
        .orElseThrow(() -> new NoSuchContentException(request.contentId().toString()));

    return null;

  }

  // Todo : UserRepository에서 User id 가져오기
  @Transactional
  public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request) {

    Review review = reviewQueryRepository.findByUuid(reviewId)
        .orElseThrow(() -> new NoSuchReviewException(reviewId.toString()));

    review.update(request.text(), request.rating());

    return ReviewDto.of(
      review.getUuid(),
      review.getContent().getUuid(),
      AuthorDto.of(UUID.randomUUID(), null, null), // 임시 User id
      review.getText(),
      review.getRating()
    );

  }

  @Transactional
  public void deleteReview(UUID reviewId) {

    if (!reviewQueryRepository.existsByUuid(reviewId)) {
      throw new NoSuchReviewException(reviewId.toString());
    }

    reviewCommandRepository.deleteByUuid(reviewId);

  }

}
