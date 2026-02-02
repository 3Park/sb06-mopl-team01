package org.example.mopl.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ContentResult;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentType;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.content.entity.QContentTag;
import org.example.mopl.content.entity.QContentsStat;
import org.example.mopl.content.entity.QReview;
import org.example.mopl.content.entity.QTag;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final WatchTogetherService watchTogetherService;

  public boolean existsByUuid(UUID uuid) {
    return queryFactory.selectFrom(QContent.content)
        .where(QContent.content.uuid.eq(uuid))
        .fetchFirst() != null;
  }

  @Transactional(readOnly = true)
  public Optional<Content> findByUuid(UUID uuid) {
    return Optional.ofNullable(
        queryFactory.selectFrom(QContent.content)
            .where(QContent.content.uuid.eq(uuid))
            .fetchOne()
    );
  }

  @Transactional(readOnly = true)
  public Optional<ContentDto> findByUuidWithContentTag(UUID uuid) {

    Content content = queryFactory.selectFrom(QContent.content)
        .where(QContent.content.uuid.eq(uuid))
        .fetchOne();

    List<ContentTag> contentTagList = queryFactory.selectFrom(QContentTag.contentTag)
        .innerJoin(QContentTag.contentTag.tag, QTag.tag)
        .fetchJoin()
        .where(QContentTag.contentTag.content.uuid.eq(uuid))
        .fetch();

    ReviewStat reviewStat = queryFactory.select(
            Projections.constructor(
                ReviewStat.class,
                QReview.review.rating.sum(),
                QReview.review.rating.count()
            )
        )
        .from(QReview.review)
        .where(QReview.review.content.uuid.eq(uuid))
        .fetchOne();

    if (content == null || reviewStat == null) {
      return Optional.empty();
    }

    return Optional.of(ContentDto.of(
        content.getUuid(),
        content.getContentType().getValue(), // ContentType의 getValue() 사용
        content.getTitle(),
        content.getDescription(),
        content.getThumbnailUrl(),
        contentTagList.stream().map(
            tag -> tag.getTag().getName()
        ).toList(
        ), // tags는 별도 조회 필요
        (double) (reviewStat.sum / reviewStat.count),
        Long.valueOf(reviewStat.count),
        watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
    ));

  }

  // V1: Content 엔티티 전체 조회
  /*public Page<Content> findAllByCursor(CursorRequestContentDto request) {

    List<Content> contentList = queryFactory.selectFrom(QContent.content)
        .join(QContentsStat.contentsStat)
        .on(QContentsStat.contentsStat.content.id.eq(QContent.content.id))
        .where(buildDynamicQueryByCursor(request))
        .orderBy(buildOrderBy(request).toArray(OrderSpecifier[]::new))
        .limit(request.limit() + 1)
        .fetch();

    boolean hasNext = contentList.size() > request.limit();

    if (hasNext) {
      contentList.remove(contentList.size() - 1);
    }

    return new PageImpl<>(
        contentList,
        Pageable.ofSize(request.limit()),
        hasNext ? request.limit() + 1 : contentList.size()
    );
  }*/

  // V2: 필요한 필드만 조회
  // One-to-one 매핑된 ContentsStat의 필드도 함께 조회
  public Page<ContentResult> findAllByCursor(CursorRequestContentDto request) {

    List<ContentResult> contentList = queryFactory.select(
            Projections.constructor(
                ContentResult.class,
                QContent.content.id,
                QContent.content.uuid,
                QContent.content.contentType.stringValue(),
                QContent.content.title,
                QContent.content.description,
                QContent.content.thumbnailUrl,
                QContent.content.createdAt,
                QContent.content.updatedAt,
                QContentsStat.contentsStat.ratingAverage,
                QContentsStat.contentsStat.ratingCount
            )
        )
        .from(QContent.content)
        .join(QContentsStat.contentsStat)
        .on(QContentsStat.contentsStat.content.id.eq(QContent.content.id))
        .where(buildDynamicQueryByCursor(request))
        .orderBy(buildOrderBy(request).toArray(OrderSpecifier[]::new))
        .limit(request.limit() + 1)
        .fetch();

    boolean hasNext = contentList.size() > request.limit();

    if (hasNext) {
      contentList.remove(contentList.size() - 1);
    }

    return new PageImpl<>(
        contentList,
        Pageable.ofSize(request.limit()),
        hasNext ? request.limit() + 1 : contentList.size()
    );

  }

  private BooleanBuilder buildDynamicQueryByCursor(CursorRequestContentDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 콘텐츠 타입
    if (request.typeEqual() != null) {
      builder.and(QContent.content.contentType.eq(ContentType.valueOf(request.typeEqual())));
    }

    // 검색 키워드
    builder.and(QContent.content.title.containsIgnoreCase(request.keywordLike()));

    if (!request.tagsIn().isEmpty()) {

      List<Long> contentIdsWithAllTags = queryFactory
          .select(QContentTag.contentTag.content.id)
          .from(QContentTag.contentTag)
          .join(QContentTag.contentTag.tag, QTag.tag)
          .where(QTag.tag.name.in(request.tagsIn()))
          .groupBy(QContentTag.contentTag.content.id)
          .having(QContentTag.contentTag.content.id.count().eq((long) request.tagsIn().size()))
          .fetch();

      if (!contentIdsWithAllTags.isEmpty()) {
        builder.and(QContent.content.id.in(contentIdsWithAllTags));
      } else {
        // 조건에 맞는 콘텐츠가 없을 경우 빈 결과를 반환하기 위해 항상 거짓인 조건 추가
        builder.and(QContent.content.id.eq(-1L));
      }

    }

    // 커서 : createdAt, watcherCount, rate
    // 보조 커서 : uuid
    if (request.sortDirection().equals("DESCENDING")) {
      switch (request.sortBy()) {
        case "watcherCount":
          // ToDo: 구현 필요
          break;
        case "rate":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QContentsStat.contentsStat.ratingAverage.lt(Double.parseDouble(request.cursor()))
                    .or(QContentsStat.contentsStat.ratingAverage.eq(
                            Double.parseDouble(request.cursor()))
                        .and(QContent.content.uuid.lt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(
                QContentsStat.contentsStat.ratingAverage.lt(Double.parseDouble(request.cursor())));
          }
          break;
        case "createdAt":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QContent.content.createdAt.lt(Instant.parse(request.cursor()))
                    .or(QContent.content.createdAt.eq(Instant.parse(request.cursor()))
                        .and(QContent.content.uuid.lt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QContent.content.createdAt.lt(Instant.parse(request.cursor())));
          }
          break;
        default:
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    } else {
      switch (request.sortBy()) {
        case "watcherCount":
          // ToDo: 구현 필요
          break;
        case "rate":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QContentsStat.contentsStat.ratingAverage.gt(Double.parseDouble(request.cursor()))
                    .or(QContentsStat.contentsStat.ratingAverage.eq(
                            Double.parseDouble(request.cursor()))
                        .and(QContent.content.uuid.gt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(
                QContentsStat.contentsStat.ratingAverage.gt(Double.parseDouble(request.cursor())));
          }
          break;
        case "createdAt":
          if (request.cursor() != null && request.idAfter() != null) {
            builder.and(
                QContent.content.createdAt.gt(Instant.parse(request.cursor()))
                    .or(QContent.content.createdAt.eq(Instant.parse(request.cursor()))
                        .and(QContent.content.uuid.gt(request.idAfter())))
            );
          } else if (request.cursor() != null) {
            // 첫 페이지
            builder.and(QContent.content.createdAt.gt(Instant.parse(request.cursor())));
          }
          break;
        default:
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    }

    return builder;

  }

  private List<OrderSpecifier<?>> buildOrderBy(CursorRequestContentDto request) {

    List<OrderSpecifier<?>> orders = new ArrayList<>();

    // 1차 정렬: createdAt, watcherCount, rate
    if (request.sortDirection().equals("DESCENDING")) {

      switch (request.sortBy()) {
        case "watcherCount":
          // ToDo: 구현 필요
          break;
        case "rate":
          orders.add(QContentsStat.contentsStat.ratingAverage.desc());
          break;
        default:
          orders.add(QContent.content.createdAt.desc());
          break;
      }

    } else {

      switch (request.sortBy()) {
        case "watcherCount":
          // ToDo: 구현 필요
          break;
        case "rate":
          orders.add(QContentsStat.contentsStat.ratingAverage.asc());
          break;
        default:
          orders.add(QContent.content.createdAt.asc());
          break;
      }

    }

    // 2차 정렬: uuid
    if (request.sortDirection().equals("DESCENDING")) {
      orders.add(QContent.content.uuid.desc());
    } else {
      orders.add(QContent.content.uuid.asc());
    }

    return orders;

  }

  private record ReviewStat(
      Long sum,
      Integer count
  ) {

  }

}
