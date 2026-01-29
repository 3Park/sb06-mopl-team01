package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
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

}
