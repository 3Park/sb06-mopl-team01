package org.example.mopl.user.repository;

import org.example.mopl.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


//PJG 추후 쿼리DSL로 변경 필요
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("""
        select distinct u from User u
                join fetch u.userRoles ur
                join fetch u.profile p
                join fetch ur.role
                where u.email = :email
    """)
    Optional<User> findByEmail(String email);

//    @Query("""
//        select distinct u from User u
//            join fetch u.profile p
//            join fetch u.temporaryPassword tp
//            join fetch u.userRoles ur
//            join fetch ur.role
//                where u.uuid = :uuid
//    """)
    Optional<User> findByUuid(UUID uuid);

    // 임시 페치 조인 메소드 -> 추후 변경 예정
    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u.uuid = :uuid")
    Optional<User> findUserAndProfileOnlyByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findUserAndProfileOnlyById(@Param("id") Long id);
}
