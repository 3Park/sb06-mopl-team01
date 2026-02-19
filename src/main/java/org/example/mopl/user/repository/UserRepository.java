package org.example.mopl.user.repository;

import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.querydsl.QueryDslUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


//PJG 추후 쿼리DSL로 변경 필요
public interface UserRepository extends JpaRepository<User,Long>, QueryDslUserRepository {
    boolean existsUserByEmail(String email);
    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u.id IN :ids")
    List<User> findAllWithProfileByIdIn(@Param("ids") List<Long> ids);

    Optional<User> findUserByEmail(String email);
}
