package org.example.mopl.content.repository;

import static com.querydsl.core.group.GroupBy.groupBy;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public List<Tag> findAllByNameIn(Collection<String> names) {
    return queryFactory.selectFrom(QTag.tag)
        .where(QTag.tag.name.in(names))
        .fetch();
  }

  public Map<String, Tag> findAllMapByNameIn(List<String> tagName) {

    return queryFactory.selectFrom(QTag.tag)
        .where(QTag.tag.name.in(tagName))
        .fetch()
        .stream()
        .collect(Collectors.toMap(Tag::getName, tag -> tag));

  }

}
