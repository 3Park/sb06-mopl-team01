package org.example.mopl.content.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
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

  private record ReviewStat(
      Long sum,
      Integer count
  ) {

  }

}
