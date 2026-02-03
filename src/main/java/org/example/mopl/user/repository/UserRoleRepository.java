package org.example.mopl.user.repository;

import org.example.mopl.user.entity.UserRole;
import org.example.mopl.user.enums.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole,Long> {

    @Query("""
        select count(ur) > 0 
        from UserRole ur
                join ur.user u
                join ur.role r
        where u.email = :email
            and r.name = :roleName         
                """)
    boolean existsUserWithEmailAndRole(String email, UserRoleType roleName);
}
