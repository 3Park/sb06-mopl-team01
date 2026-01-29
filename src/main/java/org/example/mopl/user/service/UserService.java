package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.entity.Role;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.entity.UserRole;
import org.example.mopl.user.entity.UserRoleType;
import org.example.mopl.user.repository.UserRepository;
import org.example.mopl.user.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    public static final String ADMIN_EMAIL = "admin@test.com";
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public void addAdmin(String password, Role adminRole) {
        if (userRoleRepository.existsUserWithEmailAndRole(ADMIN_EMAIL, UserRoleType.ADMIN))
            return;

        User user = User.builder()
                .email(ADMIN_EMAIL)
                .password(password)
                .build();

        userRepository.save(user);

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(adminRole)
                .build();

        userRoleRepository.save(userRole);

        Profile profile = Profile.builder()
                .profileImageUrl("")
                .user(user)
                .name("admin")
                .uuid(UUID.randomUUID())
                .build();

        profileRepository.save(profile);
    }
}
