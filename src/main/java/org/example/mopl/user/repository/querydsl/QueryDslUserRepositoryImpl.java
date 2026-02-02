package org.example.mopl.user.repository.querydsl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.entity.QRole;
import org.example.mopl.user.entity.QUser;
import org.example.mopl.user.entity.QUserRole;
import org.example.mopl.user.entity.User;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class QueryDslUserRepositoryImpl implements QueryDslUserRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QUser user = QUser.user;
    private final QUserRole userRole = QUserRole.userRole;
    private final QProfile profile = QProfile.profile;
    private final QRole role = QRole.role;


    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(findUserExpression()
                .where(user.email.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return Optional.ofNullable(findUserExpression()
                .where(user.uuid.eq(uuid))
                .fetchOne());
    }

    private JPAQuery<User> findUserExpression()
    {
        return jpaQueryFactory
                .selectDistinct(user)
                .from(user)
                .join(user.userRoles, userRole).fetchJoin()
                .join(user.profile,profile).fetchJoin()
                .join(userRole.role,role).fetchJoin();
    }
}
