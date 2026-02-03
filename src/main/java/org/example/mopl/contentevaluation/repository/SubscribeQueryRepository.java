package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
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

  public List<UUID> findSubscribersUuidsByPlaylistId(Long playlistId) {
    return queryFactory
        .select(QSubscribe.subscribe.user.uuid)
        .from(QSubscribe.subscribe)
        .where(QSubscribe.subscribe.playlist.id.eq(playlistId))
        .fetch();
  }

}
