package org.example.mopl.content.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.dto.request.ReviewCreateRequest;
import org.example.mopl.content.dto.request.ReviewUpdateRequest;
import org.example.mopl.content.dto.response.AuthorDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.Review;
import org.example.mopl.content.event.RatingEvent;
import org.example.mopl.content.exception.ContentErrorCode;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ReviewCommandRepository;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCommandService {

  private final ContentQueryRepository contentQueryRepository;
  private final ReviewCommandRepository reviewCommandRepository;
  private final ReviewQueryRepository reviewQueryRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public ReviewDto createReview(String email, ReviewCreateRequest request) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    Content content = contentQueryRepository.findByUuid(request.contentId())
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

    if (reviewQueryRepository.existsByContentIdAndUserId(content.getId(), user.getId())) {
      throw new ContentException(ContentErrorCode.DUPLICATE_REVIEW);
    }

    Review review = reviewCommandRepository.save(
        Review.of(
            user,
            content,
            request.rating(),
            request.text()
        )
    );

    // 평점 증가 이벤트 발행
    eventPublisher.publishEvent(
        RatingEvent.IncreaseRatingEvent.of(content.getId(), content.getUuid(), request.rating())
    );

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
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_REVIEW));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    // 작성자 본인이나 관리자가 아닌 경우 예외 발생
    if (!isAdmin && !review.getUser().getUuid().equals(user.getUuid())) {
      throw new ContentException(ContentErrorCode.UNAUTHORIZED_REVIEW);
    }

    // 기존 평점 삭제 이벤트 발행
    eventPublisher.publishEvent(
        RatingEvent.DecreaseRatingEvent.of(
            review.getContent().getId(),
            review.getUuid(),
            review.getRating())
    );

    // 리뷰 정보 업데이트
    review.update(request.text(), request.rating());

    // 변경된 리뷰의 평점 추가 이벤트 발행
    eventPublisher.publishEvent(
        RatingEvent.IncreaseRatingEvent.of(
            review.getContent().getId(),
            review.getUuid(),
            request.rating())
    );

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
  public void deleteReviewByUuid(String email, UUID reviewId) {

    Review review = reviewQueryRepository.findByUuid(reviewId)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_REVIEW));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    // 작성자 본인이나 관리자가 아닌 경우 예외 발생
    if (!isAdmin && !review.getUser().getUuid().equals(user.getUuid())) {
      throw new ContentException(ContentErrorCode.UNAUTHORIZED_REVIEW);
    }

    // 평점 감소 이벤트 발행
    eventPublisher.publishEvent(
        RatingEvent.DecreaseRatingEvent.of(review.getContent().getId(), review.getContent().getUuid(), review.getRating())
    );

    reviewCommandRepository.deleteByUuid(reviewId);

  }

}
