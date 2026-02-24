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

    long countByFolloweeId(Long followeeId);

    Optional<Follow> findByUuid(UUID uuid);

    /* 플레이리스트 생성 알림 등: followee(내가)를 팔로우하는 사람 목록 + follower 조인 */
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.followee.id = :followeeId")
    List<Follow> findAllByFolloweeIdWithFollower(@Param("followeeId") Long followeeId);
}
