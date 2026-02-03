package org.example.mopl.user.repository.querydsl;

import org.example.mopl.user.dto.request.UserCursorRequest;
import org.example.mopl.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueryDslUserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    List<User> findAllUsers(UserCursorRequest request);
    //N+1을 해결하기 위한 페이지네이션 후속 쿼리
    List<User> findUsersByIds(List<UUID> ids);
}
