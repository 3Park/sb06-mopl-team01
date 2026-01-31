package org.example.mopl.content.repository;

import static com.querydsl.core.group.GroupBy.groupBy;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.QTag;
import org.example.mopl.content.entity.Tag;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TagQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByName(String name) {
    return queryFactory.from(QTag.tag)
        .where(QTag.tag.name.eq(name))
        .fetchFirst() != null;
  }

  public Optional<Tag> findByName(String name) {
    return Optional.ofNullable(queryFactory.selectFrom(QTag.tag)
        .where(QTag.tag.name.eq(name))
        .fetchOne());
  }

  public Map<String, Tag> findAllByTagNames(List<String> tagName) {

    return queryFactory.from(QTag.tag)
        .where(QTag.tag.name.in(tagName))
        .transform(
            groupBy(QTag.tag.name)
                .as(QTag.tag)
        );

  }

}
