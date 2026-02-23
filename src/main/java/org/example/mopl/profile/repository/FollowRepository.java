package org.example.mopl.profile.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.mopl.profile.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    long countByFolloweeId(Long followeeId);

    Optional<Follow> findByUuid(UUID uuid);
}
