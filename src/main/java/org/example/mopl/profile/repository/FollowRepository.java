package org.example.mopl.profile.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.mopl.profile.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    long countByFolloweeId(Long followeeId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.followee JOIN FETCH f.followee.profile WHERE f.follower.id = :followerId ORDER BY f.createdAt DESC")
    List<Follow> findAllByFollowerIdWithFollowee(@Param("followerId") Long followerId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.follower JOIN FETCH f.follower.profile WHERE f.followee.id = :followeeId ORDER BY f.createdAt DESC")
    List<Follow> findAllByFolloweeIdWithFollower(@Param("followeeId") Long followeeId);

    Optional<Follow> findByUuid(UUID uuid);
}
