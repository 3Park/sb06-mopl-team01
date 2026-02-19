package org.example.mopl.profile.repository;

import org.example.mopl.profile.entity.WatchingSession;
import org.springframework.stereotype.Repository;
import org.example.mopl.profile.entity.WatchingSession.WatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface WatchingSessionRepository extends JpaRepository<WatchingSession, Long> {

    @Query("SELECT ws FROM WatchingSession ws " +
           "JOIN FETCH ws.content c " +
           "WHERE ws.watcher.id = :watcherId " +
           "AND ws.status IN (:statuses) " +
           "ORDER BY ws.updatedAt DESC, ws.createdAt DESC")
    List<WatchingSession> findByWatcherIdAndStatusIn(
        @Param("watcherId") Long watcherId,
        @Param("statuses") List<WatchingStatus> statuses
    );

    default List<WatchingSession> findActiveByWatcherId(Long watcherId) {
        return findByWatcherIdAndStatusIn(watcherId, List.of(WatchingStatus.WATCHING, WatchingStatus.PAUSED));
    }
}
