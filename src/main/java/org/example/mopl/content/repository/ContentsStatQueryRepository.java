package org.example.mopl.content.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.QContentsStat;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentsStatQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<ContentsStat> getContentsStatByContentId(Long id) {

    ContentsStat contentsStat = queryFactory
        .selectFrom(QContentsStat.contentsStat)
        .where(QContentsStat.contentsStat.content.id.eq(id))
        .fetchOne();

    return Optional.ofNullable(contentsStat);

  }

  public Map<Long, ContentsStat> getContentsStatByContentIds(List<Long> ids) {

    List<ContentsStat> contentsStatList = queryFactory
        .selectFrom(QContentsStat.contentsStat)
        .where(QContentsStat.contentsStat.content.id.in(ids))
        .fetch();

    return contentsStatList.stream()
        .collect(Collectors.toMap(
            contentsStat -> contentsStat.getContent().getId(),
            contentsStat -> contentsStat
        ));

  }

}
