package org.example.mopl.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.content.entity.QReview;
import org.example.mopl.content.entity.Review;
import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  public Map<Long, Long> countReviewsByContentIds(List<Long> contentIds) {

    List<Tuple> results = queryFactory.select(
            QReview.review.content.id,
            QReview.review.count()
        )
        .from(QReview.review)
        .where(QReview.review.content.id.in(contentIds))
        .groupBy(QReview.review.content.id)
        .fetch();

    return results.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(QReview.review.content.id),
            tuple -> tuple.get(QReview.review.count())
        ));

  }

  public Page<Review> findAllByCursor(CursorRequestReviewDto request) {

    List<Review> reviewList = queryFactory.selectFrom(QReview.review)
        .join(QReview.review.content, QContent.content)
        .fetchJoin()
        .join(QReview.review.user, QUser.user)
        .fetchJoin()
        .join(QReview.review.user.profile, QProfile.profile)
        .fetchJoin()
        .where(buildDynamicQueryByCursor(request))
        .orderBy(buildOrderBy(request).toArray(OrderSpecifier[]::new))
        .limit(request.limit() + 1)
        .fetch();

    boolean hasNext = reviewList.size() > request.limit();

    if  (hasNext) {
      reviewList.remove(reviewList.size() - 1);
    }

    return new PageImpl<>(
        reviewList,
        Pageable.ofSize(request.limit()),
        hasNext ? reviewList.size() + 1 : reviewList.size()
    );

  }

  public BooleanBuilder buildDynamicQueryByCursor(CursorRequestReviewDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 콘텐츠 id
    if (request.contentId() != null) {
      builder.and(QReview.review.content.uuid.eq(request.contentId()));
    }

    // 커서 : createdAt, rating
    // 보조 커서 : uuid
    if (request.sortDirection().equals("DESCENDING")) {

      switch (request.sortBy()) {
        case "rating":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QReview.review.rating.lt(Double.parseDouble(request.cursor()))
                    .or(QReview.review.rating.eq(Double.parseDouble(request.cursor()))
                        .and(QReview.review.uuid.lt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QReview.review.rating.lt(Double.parseDouble(request.cursor())));
          }
          break;
        case "createdAt":
          if  (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QReview.review.createdAt.lt(Instant.parse(request.cursor()))
                    .or(QReview.review.createdAt.eq(Instant.parse(request.cursor()))
                        .and(QReview.review.uuid.lt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QReview.review.createdAt.lt(Instant.parse(request.cursor())));
          }
          break;
        default:
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }

    } else {

      switch (request.sortBy()) {
        case "rating":
          if  (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QReview.review.rating.gt(Double.parseDouble(request.cursor()))
                    .or(QReview.review.rating.eq(Double.parseDouble(request.cursor()))
                        .and(QReview.review.uuid.gt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QReview.review.rating.gt(Double.parseDouble(request.cursor())));
          }
          break;
        case "createdAt":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QReview.review.createdAt.gt(Instant.parse(request.cursor()))
                    .or(QReview.review.createdAt.eq(Instant.parse(request.cursor()))
                        .and(QReview.review.uuid.gt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QReview.review.createdAt.gt(Instant.parse(request.cursor())));
          }
          break;
        default:
          throw new IllegalArgumentException("Invalid sortBy field: " + request.sortBy());
      }

    }

    return builder;

  }

  private List<OrderSpecifier<?>> buildOrderBy(CursorRequestReviewDto request) {

    List<OrderSpecifier<?>> orders = new ArrayList<>();

    // 1차 정렬: rating, createdAt
    if (request.sortDirection().equals("DESCENDING")) {
      orders.add(request.sortBy().equals("rating") ?
          QReview.review.rating.desc() :
          QReview.review.createdAt.desc());
    } else {
      orders.add(request.sortBy().equals("rating") ?
          QReview.review.rating.asc() :
          QReview.review.createdAt.asc());
    }

    // 2차 정렬: 항상 uuid
    orders.add(request.sortDirection().equals("DESCENDING") ?
        QReview.review.uuid.desc() :
        QReview.review.uuid.asc());

    return orders;

  }

}
