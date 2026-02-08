package org.example.mopl.content.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.QContentTag;
import org.example.mopl.content.entity.QTag;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentTagQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByContentIdAndTagId(Long contentId, Long tagId) {
    Integer count = queryFactory.selectOne()
        .from(QContentTag.contentTag)
        .where(
            QContentTag.contentTag.content.id.eq(contentId),
            QContentTag.contentTag.tag.id.eq(tagId)
        )
        .fetchFirst();

    return count != null;
  }

  public Set<String> findTagNamesByContentId(Long contentId) {
    List<String> tagNames = queryFactory.select(
            QTag.tag.name
        )
        .from(QContentTag.contentTag)
        .innerJoin(QContentTag.contentTag.tag, QTag.tag)
        .where(QContentTag.contentTag.content.id.eq(contentId))
        .fetch();

    return Set.copyOf(tagNames);
  }

  public Map<Long, List<String>> findTagsByContentIds(List<Long> contentIds) {
    List<Tuple> tagTuples = queryFactory.select(
            QContentTag.contentTag.content.id,
            QTag.tag.name
        )
        .from(QContentTag.contentTag)
        .innerJoin(QContentTag.contentTag.tag, QTag.tag)
        .where(QContentTag.contentTag.content.id.in(contentIds))
        .fetch();

    return tagTuples.stream()
        .collect(Collectors.groupingBy(
            tuple -> tuple.get(QContentTag.contentTag.content.id),
            Collectors.mapping(tuple -> tuple.get(QTag.tag.name), Collectors.toList())
        ));
  }

}
