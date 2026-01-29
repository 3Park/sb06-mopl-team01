package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.user.entity.Role;
import org.example.mopl.user.entity.UserRoleType;
import org.example.mopl.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Transactional
    public void addDefaultRole() {
        if(roleRepository.existsByName(UserRoleType.ADMIN) == false)
            roleRepository.save(createRole(UserRoleType.ADMIN,true));

        if(roleRepository.existsByName(UserRoleType.USER) == false)
            roleRepository.save(createRole(UserRoleType.USER,false));
    }

    @Transactional(readOnly = true)
    public Role getAdminRole()
    {
        return roleRepository.findByName(UserRoleType.ADMIN).orElse(null);
    }

    private Role createRole(UserRoleType userRoleType, boolean isAdmin) {
        Role role = Role.builder()
                .name(userRoleType)
                .isAdmin(isAdmin).build();

        return role;
    }
}
