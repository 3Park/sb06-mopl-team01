package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaylistContentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByPlaylistIdAndContentId(Long playlistId, Long contentId) {
    Integer fetchOne = queryFactory
        .selectOne()
        .from(org.example.mopl.contentevaluation.entity.QPlaylistContent.playlistContent)
        .where(
            org.example.mopl.contentevaluation.entity.QPlaylistContent.playlistContent.playlist.id.eq(playlistId)
                .and(org.example.mopl.contentevaluation.entity.QPlaylistContent.playlistContent.content.id.eq(contentId))
        )
        .fetchFirst();
    return fetchOne != null;
  }

}
