package org.example.mopl.content.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.content.entity.QReview;
import org.example.mopl.content.entity.Review;
import org.springframework.stereotype.Repository;

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

}
