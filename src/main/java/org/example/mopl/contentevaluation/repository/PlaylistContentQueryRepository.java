package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.QContent;
import org.example.mopl.contentevaluation.entity.PlaylistContent;
import org.example.mopl.contentevaluation.entity.QPlaylistContent;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaylistContentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByPlaylistIdAndContentId(Long playlistId, Long contentId) {
    Integer fetchOne = queryFactory
        .selectOne()
        .from(QPlaylistContent.playlistContent)
        .where(QPlaylistContent.playlistContent.playlist.id.eq(playlistId)
                .and(QPlaylistContent.playlistContent.content.id.eq(contentId))
        )
        .fetchFirst();
    return fetchOne != null;
  }

  public List<PlaylistContent> findAllByPlaylistId(Long playlistId) {
    return queryFactory
        .selectFrom(QPlaylistContent.playlistContent)
        .join(QPlaylistContent.playlistContent.content, QContent.content)
        .fetchJoin()
        .where(QPlaylistContent.playlistContent.playlist.id.eq(playlistId))
        .fetch();
  }

  public Map<Long, List<PlaylistContent>> findAllByPlaylistIds(List<Long> playlistIds) {
    List<PlaylistContent> playlistContents = queryFactory
        .selectFrom(QPlaylistContent.playlistContent)
        .join(QPlaylistContent.playlistContent.content, QContent.content)
        .fetchJoin()
        .where(QPlaylistContent.playlistContent.playlist.id.in(playlistIds))
        .fetch();

    return playlistContents.stream()
        .collect(
            Collectors.groupingBy(
                playlistContent -> playlistContent.getPlaylist().getId(),
                Collectors.toList()
            )
        );
  }

}
