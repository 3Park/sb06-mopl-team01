package org.example.mopl.profile.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.entity.QPlaylist;
import org.example.mopl.contentevaluation.entity.QPlaylistsStat;
import org.example.mopl.contentevaluation.entity.QSubscribe;
import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.entity.QUser;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscribedPlaylistQueryRepository {

    private final JPAQueryFactory queryFactory;

    public record SubscribedPlaylistRow(
            UUID playlistUuid,
            String title,
            String description,
            UUID ownerUuid,
            String ownerName,
            String ownerProfileImageUrl,
            Long subscriberCount,
            Instant updatedAt
    ) {}

    /**
     * 프로필 주인(userId)이 구독한 플레이리스트 목록을 Subscribe 테이블 기준으로 조회 (프로필 도메인 전용).
     */
    public CursorResult<SubscribedPlaylistRow> findSubscribedByUserId(
            Long userId,
            int limit,
            String sortBy,
            String sortDirection,
            Instant cursor,
            UUID idAfter
    ) {
        boolean descending = "DESCENDING".equalsIgnoreCase(sortDirection);

        var query = queryFactory
                .select(
                        Projections.constructor(
                                SubscribedPlaylistRow.class,
                                QPlaylist.playlist.uuid,
                                QPlaylist.playlist.title,
                                QPlaylist.playlist.description,
                                QUser.user.uuid,
                                QProfile.profile.name,
                                QProfile.profile.profileImageUrl,
                                QPlaylistsStat.playlistsStat.subscribeCount,
                                QPlaylist.playlist.updatedAt
                        )
                )
                .from(QSubscribe.subscribe)
                .join(QSubscribe.subscribe.playlist, QPlaylist.playlist)
                .join(QPlaylist.playlist.user, QUser.user)
                .join(QUser.user.profile, QProfile.profile)
                .leftJoin(QPlaylistsStat.playlistsStat)
                .on(QPlaylistsStat.playlistsStat.playlist.eq(QPlaylist.playlist))
                .where(QSubscribe.subscribe.user.id.eq(userId));

        if (cursor != null) {
            if (descending) {
                if (idAfter != null) {
                    query = query.where(
                            QPlaylist.playlist.updatedAt.lt(cursor)
                                    .or(QPlaylist.playlist.updatedAt.eq(cursor)
                                            .and(QPlaylist.playlist.uuid.lt(idAfter)))
                    );
                } else {
                    query = query.where(QPlaylist.playlist.updatedAt.lt(cursor));
                }
            } else {
                if (idAfter != null) {
                    query = query.where(
                            QPlaylist.playlist.updatedAt.gt(cursor)
                                    .or(QPlaylist.playlist.updatedAt.eq(cursor)
                                            .and(QPlaylist.playlist.uuid.gt(idAfter)))
                    );
                } else {
                    query = query.where(QPlaylist.playlist.updatedAt.gt(cursor));
                }
            }
        }

        if (descending) {
            query = query.orderBy(QPlaylist.playlist.updatedAt.desc(), QPlaylist.playlist.uuid.desc());
        } else {
            query = query.orderBy(QPlaylist.playlist.updatedAt.asc(), QPlaylist.playlist.uuid.asc());
        }

        List<SubscribedPlaylistRow> list = query.limit(limit + 1).fetch();

        boolean hasNext = list.size() > limit;
        if (hasNext) {
            list = list.subList(0, limit);
        }

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext && !list.isEmpty()) {
            SubscribedPlaylistRow last = list.get(list.size() - 1);
            nextCursor = last.updatedAt().toString();
            nextIdAfter = last.playlistUuid();
        }

        return new CursorResult<>(list, nextCursor, nextIdAfter, hasNext);
    }

    public record CursorResult<T>(List<T> data, String nextCursor, UUID nextIdAfter, boolean hasNext) {}
}
