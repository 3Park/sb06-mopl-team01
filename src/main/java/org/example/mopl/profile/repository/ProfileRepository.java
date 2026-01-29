package org.example.mopl.profile.repository;

import org.example.mopl.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId")
    Optional<Profile> findByUserId(@Param("userId") Long userId);


    Optional<Profile> findByUuid(UUID uuid);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    default boolean existsByUserId(Long userId) {
        return countByUserId(userId) > 0;
    }
}
