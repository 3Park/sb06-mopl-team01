package org.example.mopl.user.repository;

import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.querydsl.QueryDslUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;


//PJG 추후 쿼리DSL로 변경 필요
public interface UserRepository extends JpaRepository<User,Long>, QueryDslUserRepository {
    boolean existsUserByEmail(String email);
}
