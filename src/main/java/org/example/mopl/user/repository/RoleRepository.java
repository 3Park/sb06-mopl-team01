package org.example.mopl.user.repository;

import org.example.mopl.user.entity.Role;
import org.example.mopl.user.enums.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByName(UserRoleType name);
    Optional<Role> findByName(UserRoleType name);
}
