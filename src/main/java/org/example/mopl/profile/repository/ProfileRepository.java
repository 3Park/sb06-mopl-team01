package org.example.mopl.profile.repository;

import org.example.mopl.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {


    // User ID로 프로필 조회
    Optional<Profile> findByUserId(Long userId);

    // UUID로 프로필 조회
    Optional<Profile> findByUuid(UUID uuid);

    // 프로필 존재 여부 확인
    boolean existsByUserId(Long userId);
}
