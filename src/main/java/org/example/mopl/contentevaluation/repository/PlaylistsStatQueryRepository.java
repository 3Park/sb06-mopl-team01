package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.entity.QPlaylistsStat;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaylistsStatQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<PlaylistsStat> findByPlaylistId(Long playlistId) {
    return Optional.ofNullable(
        queryFactory.selectFrom(QPlaylistsStat.playlistsStat)
            .where(QPlaylistsStat.playlistsStat.playlist.id.eq(playlistId))
            .fetchOne()
    );
  }

  public Map<Long, PlaylistsStat> findAllMapByPlaylistIds(List<Long> playlistIds) {
    List<PlaylistsStat> playlistsStats = queryFactory
        .selectFrom(QPlaylistsStat.playlistsStat)
        .where(QPlaylistsStat.playlistsStat.playlist.id.in(playlistIds))
        .fetch();

    return playlistsStats.stream()
        .collect(
            Collectors.toMap(
                playlistsStat -> playlistsStat.getPlaylist().getId(),
                playlistsStat -> playlistsStat
            )
        );
  }

}
