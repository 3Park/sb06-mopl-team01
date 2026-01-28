package org.example.mopl.content.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.QContent;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<Content> findByUuid(UUID uuid) {
    return Optional.ofNullable(
        queryFactory.selectFrom(QContent.content)
            .where(QContent.content.uuid.eq(uuid))
            .fetchOne()
    );
  }

}
