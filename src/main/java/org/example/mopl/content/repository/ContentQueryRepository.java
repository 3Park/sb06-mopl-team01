package org.example.mopl.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentType;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.content.entity.QContentTag;
import org.example.mopl.content.entity.QReview;
import org.example.mopl.content.entity.QTag;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepository {

  private final JPAQueryFactory queryFactory;

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

    /*long countWatcher = queryFactory.selectFrom(QPlaylistContent.playlistContent)
        .where(QPlaylistContent.playlistContent.content.uuid.eq(uuid))
        .fetch().size();*/

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
        reviewStat.count,
        0L
    ));

  }

  @Transactional(readOnly = true)
  public Page<ContentDto> findAllByCursor(CursorRequestContentDto request) {
    return null;
  }

  private BooleanBuilder buildDynamicQueryByCursor(CursorRequestContentDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 콘텐츠 타입
    if (request.typeEqual() != null) {
      builder.and(QContent.content.contentType.eq(ContentType.valueOf(request.typeEqual())));
    }

    // 검색 키워드
    builder.and(QContent.content.title.containsIgnoreCase(request.keywordLike()));

    // 정렬 방향 & 정렬 기준

    if (request.sortBy().equals("watcherCount")) {

    } else if (request.sortBy().equals("rate")) {

    } else {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QContent.content.createdAt.lt(Instant.parse(request.cursor())));
      } else {
        builder.and(QContent.content.createdAt.gt(Instant.parse(request.cursor())));
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

  private record ReviewStat(
      Long sum,
      Integer count
  ) {

  }

}
