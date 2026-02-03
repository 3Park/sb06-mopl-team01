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
}
