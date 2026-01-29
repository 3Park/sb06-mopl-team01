package org.example.mopl.contentevaluation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.entity.QPlaylist;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PlaylistQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Transactional(readOnly = true)
  public Page<PlaylistDto> findAllByCursor(CursorRequestPlaylistDto request) {
    return null;
  }

  public BooleanBuilder buildDynamicQueryByCursor(CursorRequestPlaylistDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 검색 키워드
    if (request.keywordLike() != null) {
      builder.and(QPlaylist.playlist.title.containsIgnoreCase(request.keywordLike()));
    }

    // 소유자 ID
    if (request.ownerIdEqual() != null) {
      //builder.and(QPlaylist.playlist.user);
    }

    // 구독자 ID

    // 커서

    // 보조 커서
    if (request.idAfter() != null) {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QPlaylist.playlist.uuid.lt(request.idAfter()));
      } else {
        builder.and(QPlaylist.playlist.uuid.gt(request.idAfter()));
      }

    }

    // 정렬 방향 & 정렬 기준 : updatedAt, subscribeCount
    if (request.sortBy().equals("updatedAt")) {

      if (request.sortDirection().equals("DESCENDING")) {
        builder.and(QPlaylist.playlist.updatedAt.lt(Instant.parse(request.cursor())));
      } else {
        builder.and(QPlaylist.playlist.updatedAt.gt(Instant.parse(request.cursor())));
      }

    } else if (request.sortBy().equals("subscribeCount")) {

      if (request.sortDirection().equals("DESCENDING")) {
        // Todo : 구독자 수 기준 정렬
      } else {
        // Todo : 구독자 수 기준 정렬
      }

    }


    return builder;

  }

}
