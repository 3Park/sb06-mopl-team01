package org.example.mopl.contentevaluation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.dto.ContentEvaluationQueryDto;
import org.example.mopl.contentevaluation.dto.ContentEvaluationQueryDto.CursorPlaylistPage;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.QPlaylist;
import org.example.mopl.contentevaluation.entity.QPlaylistsStat;
import org.example.mopl.contentevaluation.entity.QSubscribe;
import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaylistQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<Playlist> findByUuid(UUID uuid) {
    return Optional.ofNullable(queryFactory.selectFrom(QPlaylist.playlist)
        .join(QPlaylist.playlist.user, QUser.user)
        .fetchJoin()
        .join(QUser.user.profile, QProfile.profile)
        .fetchJoin()
        .where(QPlaylist.playlist.uuid.eq(uuid))
        .fetchOne());
  }

  public Page<Playlist> findAllByCursor(CursorRequestPlaylistDto request) {

    List<Playlist> playlists = queryFactory.selectFrom(QPlaylist.playlist)
        .leftJoin(QPlaylist.playlist.user, QUser.user)
        .fetchJoin()
        .join(QUser.user.profile, QProfile.profile)
        .fetchJoin()
        .where(buildDynamicQueryByCursor(request))
        .orderBy(buildOrderBy(request).toArray(new OrderSpecifier<?>[0]))
        .limit(request.limit() + 1)
        .fetch();

    boolean hasNext = playlists.size() > request.limit();

    if (playlists.size() > request.limit()) {
      playlists.remove(playlists.size() - 1);
    }

    return new PageImpl<>(
        playlists,
        Pageable.ofSize(request.limit()),
        hasNext ? request.limit() + 1 : playlists.size()
    );

  }

  // V2: 구독 정보 및 통계 포함
  // 구독자 ID에 따른 구독 여부 포함
  // 추후 성능 테스트 필요
  public Page<ContentEvaluationQueryDto.CursorPlaylistPage> findAllByCursorV2(CursorRequestPlaylistDto request) {

    List<CursorPlaylistPage> playlists = queryFactory.select(
            QPlaylist.playlist.id,
            QPlaylist.playlist.uuid,
            QPlaylist.playlist.user.uuid,
            QPlaylist.playlist.user.profile.name,
            QPlaylist.playlist.user.profile.profileImageUrl,
            QPlaylist.playlist.title,
            QPlaylist.playlist.description,
            QPlaylist.playlist.updatedAt,
            QPlaylistsStat.playlistsStat.subscribeCount,
            QSubscribe.subscribe.uuid
        )
        .from(QPlaylist.playlist)
        .leftJoin(QPlaylist.playlist.user, QUser.user)
        .fetchJoin()
        .join(QUser.user.profile, QProfile.profile)
        .fetchJoin()
        .leftJoin(QPlaylistsStat.playlistsStat)
        .on(QPlaylistsStat.playlistsStat.playlist.eq(QPlaylist.playlist))
        .leftJoin(QSubscribe.subscribe)
        .on(QSubscribe.subscribe.playlist.eq(QPlaylist.playlist)
            .and(QSubscribe.subscribe.user.uuid.eq(request.subscriberIdEqual() != null ? request.subscriberIdEqual() : UUID.randomUUID())))
        .where(buildDynamicQueryByCursor(request))
        .orderBy(buildOrderBy(request).toArray(new OrderSpecifier<?>[0]))
        .limit(request.limit() + 1)
        .fetch()
        .stream()
        .map(record -> new CursorPlaylistPage(
            record.get(QPlaylist.playlist.id),
            record.get(QPlaylist.playlist.uuid),
            record.get(QPlaylist.playlist.user.uuid),
            record.get(QUser.user.profile.name),
            record.get(QUser.user.profile.profileImageUrl),
            record.get(QPlaylist.playlist.title),
            record.get(QPlaylist.playlist.description),
            record.get(QPlaylist.playlist.updatedAt),
            record.get(QPlaylistsStat.playlistsStat.subscribeCount) != null ? record.get(QPlaylistsStat.playlistsStat.subscribeCount) : 0L,
            record.get(QSubscribe.subscribe.uuid) != null
        )).toList();

    boolean hasNext = playlists.size() > request.limit();

    if (playlists.size() > request.limit()) {
      playlists.remove(playlists.size() - 1);
    }

    return new PageImpl<>(
        playlists,
        Pageable.ofSize(request.limit()),
        hasNext ? request.limit() + 1 : playlists.size()
    );

  }

  public BooleanBuilder buildDynamicQueryByCursor(CursorRequestPlaylistDto request) {

    BooleanBuilder builder = new BooleanBuilder();

    // 검색 키워드
    if (request.keywordLike() != null) {
      builder.and(QPlaylist.playlist.title.containsIgnoreCase(request.keywordLike()));
    }

    // 소유자 ID
    if (request.ownerIdEqual() != null) {
      builder.and(QPlaylist.playlist.user.uuid.eq(request.ownerIdEqual()));
    }

    // 구독자 ID
    if (request.subscriberIdEqual() != null) {
      builder.and(QPlaylist.playlist.user.uuid.eq(request.subscriberIdEqual()));
    }

    // 커서 : updatedAt, subscribeCount
    // 보조 커서 : uuid
    if (request.sortDirection().equals("DESCENDING")) {
      switch (request.sortBy()) {
        case "updatedAt" :
          builder.and(
              QPlaylist.playlist.updatedAt.lt(Instant.parse(request.cursor()))
                  .or(QPlaylist.playlist.updatedAt.eq(Instant.parse(request.cursor()))
                      .and(QPlaylist.playlist.uuid.lt(request.idAfter())))
          );
          break;
        case "subscribeCount" :
          builder.and(
              QPlaylistsStat.playlistsStat.subscribeCount.lt(Long.parseLong(request.cursor()))
                  .or(QPlaylistsStat.playlistsStat.subscribeCount.eq(Long.parseLong(request.cursor()))
                      .and(QPlaylist.playlist.uuid.lt(request.idAfter())))
          );
          break;
        default :
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    } else {
      switch (request.sortBy()) {
        case "updatedAt" :
          builder.and(
              QPlaylist.playlist.updatedAt.gt(Instant.parse(request.cursor()))
                  .or(QPlaylist.playlist.updatedAt.eq(Instant.parse(request.cursor()))
                      .and(QPlaylist.playlist.uuid.gt(request.idAfter())))
          );
          break;
        case "subscribeCount" :
          builder.and(
              QPlaylistsStat.playlistsStat.subscribeCount.gt(Long.parseLong(request.cursor()))
                  .or(QPlaylistsStat.playlistsStat.subscribeCount.eq(Long.parseLong(request.cursor()))
                      .and(QPlaylist.playlist.uuid.gt(request.idAfter())))
          );
          break;
        default :
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    }

    return builder;

  }

  private List<OrderSpecifier<?>> buildOrderBy(CursorRequestPlaylistDto request) {

    List<OrderSpecifier<?>> orders = new ArrayList<>();

    // 1차 정렬: updatedAt, subscribeCount
    if (request.sortDirection().equals("DESCENDING")) {
      switch (request.sortBy()) {
        case "updatedAt" :
          orders.add(QPlaylist.playlist.updatedAt.desc());
          break;
        case "subscribeCount" :
          orders.add(QPlaylistsStat.playlistsStat.subscribeCount.desc());
          break;
        default :
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    } else {
      switch (request.sortBy()) {
        case "updatedAt" :
          orders.add(QPlaylist.playlist.updatedAt.asc());
          break;
        case "subscribeCount" :
          orders.add(QPlaylistsStat.playlistsStat.subscribeCount.asc());
          break;
        default :
          throw new IllegalArgumentException("잘못된 검색 조건입니다: " + request.sortBy());
      }
    }

    // 2차 정렬: uuid
    if (request.sortDirection().equals("DESCENDING")) {
      orders.add(QPlaylist.playlist.uuid.desc());
    } else {
      orders.add(QPlaylist.playlist.uuid.asc());
    }

    return orders;

  }

}
