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
import org.example.mopl.content.exception.UnauthorizedReviewException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ReviewCommandRepository;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.entity.UserRoleType;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {

  private final ContentQueryRepository contentQueryRepository;
  private final ReviewCommandRepository reviewCommandRepository;
  private final ReviewQueryRepository reviewQueryRepository;
  private final UserRepository userRepository;

  @Transactional
  public ReviewDto createReview(String email, ReviewCreateRequest request) {

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("No such user with email: " + email));

    Content content = contentQueryRepository.findByUuid(request.contentId())
        .orElseThrow(() -> new NoSuchContentException(request.contentId().toString()));

    Review review = reviewCommandRepository.save(
        Review.of(
            user,
            content,
            request.rating(),
            request.text()
        )
    );

    // Todo : 콘텐츠 통계테이블 갱신 이벤트 발행

    return ReviewDto.of(
      review.getUuid(),
      content.getUuid(),
      AuthorDto.of(
        user.getUuid(),
        user.getProfile().getName(),
        user.getProfile().getProfileImageUrl()
      ),
      review.getText(),
      review.getRating()
    );

  }

  @Transactional
  public ReviewDto updateReview(String email, UUID reviewId, ReviewUpdateRequest request) {

    Review review = reviewQueryRepository.findByUuid(reviewId)
        .orElseThrow(() -> new NoSuchReviewException(reviewId.toString()));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("No such user with email: " + email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !review.getUser().getUuid().equals(user.getUuid())) {
      throw new UnauthorizedReviewException(email);
    }

    review.update(request.text(), request.rating());

    return ReviewDto.of(
      review.getUuid(),
      review.getContent().getUuid(),
      AuthorDto.of(
        user.getUuid(),
        user.getProfile().getName(),
        user.getProfile().getProfileImageUrl()
      ),
      review.getText(),
      review.getRating()
    );

  }

  @Transactional
  public void deleteReview(String email, UUID reviewId) {

    Review review = reviewQueryRepository.findByUuid(reviewId)
        .orElseThrow(() -> new NoSuchReviewException(reviewId.toString()));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("No such user with email: " + email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !review.getUser().getUuid().equals(user.getUuid())) {
      throw new UnauthorizedReviewException(email);
    }

    reviewCommandRepository.deleteByUuid(reviewId);

  }

}
