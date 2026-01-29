package org.example.mopl.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.content.entity.QReview;
import org.example.mopl.content.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByUuid(UUID reviewId) {
    return queryFactory.selectFrom(QReview.review)
        .where(QReview.review.uuid.eq(reviewId))
        .fetchFirst() != null;
  }

  public Optional<Review> findByUuid(UUID reviewId) {
    return Optional.ofNullable(
        queryFactory.selectFrom(QReview.review)
            .innerJoin(QReview.review.content, QContent.content)
            .fetchJoin()
            .where(QReview.review.uuid.eq(reviewId))
            .fetchOne()
    );
  }

  @Transactional(readOnly = true)
  public Page<ReviewDto> findAllByCursor(CursorRequestReviewDto request) {

    return null;

  }

  public BooleanBuilder buildDynamicQueryByCursor(CursorRequestReviewDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 콘텐츠 id
    builder.and(QReview.review.content.uuid.eq(request.contentId()));

    // 정렬 방향 & 정렬 기준

    if (request.sortBy().equals("rating")) {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QReview.review.rating.lt(Double.parseDouble(request.cursor())));
      } else {
        builder.and(QReview.review.rating.gt(Double.parseDouble(request.cursor())));
      }

    } else {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QReview.review.createdAt.lt(Instant.parse(request.cursor())));
      } else {
        builder.and(QReview.review.createdAt.gt(Instant.parse(request.cursor())));
      }

    }

    // 커서

    // 보조 커서 UUID
    if (request.idAfter() != null) {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QContent.content.uuid.lt(request.idAfter()));
      } else {
        builder.and(QContent.content.uuid.gt(request.idAfter()));
      }

    }

    return builder;


  }

}
