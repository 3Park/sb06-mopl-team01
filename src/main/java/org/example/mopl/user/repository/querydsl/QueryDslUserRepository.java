package org.example.mopl.user.repository.querydsl;

import org.example.mopl.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface QueryDslUserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
}
