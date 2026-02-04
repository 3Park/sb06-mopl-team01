package org.example.mopl.user.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.profile.entity.QProfile;
import org.example.mopl.user.dto.request.UserCursorRequest;
import org.example.mopl.user.entity.QRole;
import org.example.mopl.user.entity.QUser;
import org.example.mopl.user.entity.QUserRole;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.enums.UserSortDirection;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.util.List;
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
        return Optional.ofNullable(findUserExpression(false)
                .where(user.email.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return Optional.ofNullable(findUserExpression(false)
                .where(user.uuid.eq(uuid))
                .fetchOne());
    }

    @Override
    public List<User> findAllUsers(UserCursorRequest request) {

        return findUserExpression(true)
                .where(
                        getEmailExpression(request),
                        getRoleExpression(request),
                        getLockedExpression(request),
                        cursorExpression(request)
                )
                .orderBy(orderByDirection(request), orderByDirection(request.sortDirection(), user.uuid))
                .limit(request.limit()+1)
                .fetch();
    }

    //N+1 을 해결하기 위한 페이지네이션 후속 쿼리
    @Override
    public List<User> findUsersByIds(List<UUID> ids) {
        return findUserExpression(false)
                .where(user.uuid.in(ids))
                .fetch();
    }

    @Override
    public Optional<User> findUserAndProfileOnlyById(Long id) {
        return Optional.ofNullable(findUserOnlyProfileExpression()
                .where(user.id.eq(id))
                .fetchFirst());
    }

    @Override
    public Optional<User> findUserAndProfileOnlyByUuid(UUID uuid) {
        return Optional.ofNullable(findUserOnlyProfileExpression()
                .where(user.uuid.eq(uuid))
                .fetchFirst());
    }

    private BooleanExpression cursorExpression(UserCursorRequest request) {
        if (request.cursor() == null || request.idAfter() == null) {
            return null; // 최초 페이지 → 커서 조건 없음
        }

        if(request.sortDirection() == UserSortDirection.ASCENDING)
            return cursorExpressionAsc(request);

        return cursorExpressionDesc(request);
    }

    private BooleanExpression cursorExpressionDesc(UserCursorRequest request)
    {
        switch (request.sortBy())
        {
            case name:
                return profile.name.lt(request.cursor())
                        .or(
                                profile.name.eq(request.cursor())
                                        .and(user.uuid.lt(request.idAfter()))
                        );
            case email:
                return user.email.lt(request.cursor()).
                        or(
                                user.email.eq(request.cursor())
                                        .and(user.uuid.lt(request.idAfter())
                        ));
            case role:
            case isLocked:
                return user.uuid.lt(request.idAfter());
            case createdAt:
                Instant cursorTime = Instant.parse(request.cursor());
                return user.createdAt.lt(cursorTime)
                        .or(
                                user.createdAt.eq(cursorTime)
                                        .and(user.uuid.lt(request.idAfter())
                        ));
            default:
                return null;
        }
    }

    private BooleanExpression cursorExpressionAsc(UserCursorRequest request)
    {
        switch (request.sortBy())
        {
            case name:
                return profile.name.gt(request.cursor())
                        .or(
                                profile.name.eq(request.cursor())
                                        .and(user.uuid.gt(request.idAfter()))
                        );
            case email:
                return user.email.gt(request.cursor()).
                        or(
                                user.email.eq(request.cursor())
                                        .and(user.uuid.gt(request.idAfter())
                                        ));
            case role:
            case isLocked:
                return user.uuid.gt(request.idAfter());
            case createdAt:
                Instant cursorTime = Instant.parse(request.cursor());
                return user.createdAt.gt(cursorTime)
                        .or(
                                user.createdAt.eq(cursorTime)
                                        .and(user.uuid.gt(request.idAfter())
                                        ));
            default:
                return null;
        }
    }

    private BooleanExpression getEmailExpression(UserCursorRequest request)
    {
        return StringUtils.hasText(request.emailLike()) ? user.email.like(request.emailLike()) : null;
    }

    private BooleanExpression getRoleExpression(UserCursorRequest request)
    {
        return request.roleEqual() != null ? role.name.eq(request.roleEqual()) : null;
    }

    private BooleanExpression getLockedExpression(UserCursorRequest request)
    {
        return request.isLocked() != null ? user.locked.eq(request.isLocked()) : null;
    }

    private OrderSpecifier orderByDirection(UserCursorRequest request)
    {
        switch (request.sortBy())
        {
            case name:
                return orderByDirection(request.sortDirection(), profile.name);
            case email:
                return orderByDirection(request.sortDirection(), user.email);
            case role:
                return orderByDirection(request.sortDirection(), role.name);
            case isLocked:
                return orderByDirection(request.sortDirection(), user.locked);
            case createdAt:
                return orderByDirection(request.sortDirection(), user.createdAt);
            default:
                return null;
        }
    }

    private OrderSpecifier orderByDirection(UserSortDirection direction, ComparableExpressionBase field)
    {
        if(direction == UserSortDirection.ASCENDING)
            return field.asc();

        return field.desc();
    }

    private JPAQuery<User> findUserExpression(boolean isSlice)
    {
        JPAQuery<User> query = jpaQueryFactory
                                .select(user)
                                .from(user);

        //list 인 경우, slice 할때에는 fetch join을 쓰면 안된다. (limit 등 쿼리 select 오동작)
        if(isSlice)
        {
            query.join(user.userRoles, userRole);
            query.join(user.profile,profile);
            query.join(userRole.role,role);
        }
        else
        {
            query.join(user.userRoles, userRole).fetchJoin();
            query.join(user.profile,profile).fetchJoin();
            query.join(userRole.role,role).fetchJoin();
            query.distinct();
        }

        return  query;
    }

    private JPAQuery<User> findUserOnlyProfileExpression()
    {
        return  jpaQueryFactory
                    .select(user)
                    .from(user)
                    .join(user.profile,profile).fetchJoin()
                    .distinct();
    }
}
