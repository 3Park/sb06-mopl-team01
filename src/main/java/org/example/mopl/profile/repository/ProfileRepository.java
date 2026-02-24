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

    @Query("SELECT p FROM Profile p WHERE p.user.uuid = :userUuid")
    Optional<Profile> findByUserUuid(@Param("userUuid") UUID userUuid);

    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.user.uuid = :userUuid")
    Optional<Profile> findWithUserByUserUuid(@Param("userUuid") UUID userUuid);

    @Query("SELECT DISTINCT p FROM Profile p JOIN FETCH p.user u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.uuid = :userUuid")
    Optional<Profile> findWithUserAndRolesByUserUuid(@Param("userUuid") UUID userUuid);

    Optional<Profile> findByUuid(UUID uuid);
}
