package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.entity.QSubscribe;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscribeQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId) {
    Integer fetchOne = queryFactory
        .selectOne()
        .from(QSubscribe.subscribe)
        .where(QSubscribe.subscribe.user.id.eq(userId)
                .and(QSubscribe.subscribe.playlist.id.eq(playlistId))
        )
        .fetchFirst();
    return fetchOne != null;
  }

}
