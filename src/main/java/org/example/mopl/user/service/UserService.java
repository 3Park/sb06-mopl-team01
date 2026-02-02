package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.dto.request.UserCreateRequest;
import org.example.mopl.user.entity.Role;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.entity.UserRole;
import org.example.mopl.user.entity.UserRoleType;
import org.example.mopl.user.exception.UserErrorCode;
import org.example.mopl.user.exception.UserException;
import org.example.mopl.user.repository.RoleRepository;
import org.example.mopl.user.repository.UserRepository;
import org.example.mopl.user.repository.UserRoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String ADMIN_NAME = "admin";
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordService temporaryPasswordService;

    @Transactional
    public void addAdmin(String password) {
        try
        {
            createUser(ADMIN_EMAIL,password,ADMIN_NAME,UserRoleType.ADMIN);
        }
        catch (UserException e)
        {
            if(e.getErrorCode() != UserErrorCode.DUPLICATED_USER)
                throw e;
        }
    }

    @Transactional
    public UserDto createUser(UserCreateRequest request)
    {
        return createUser(request.getEmail(), request.getPassword(), request.getName(), UserRoleType.USER);
    }

    private UserDto createUser(String email, String password, String name, UserRoleType type)
    {
        if (userRoleRepository.existsUserWithEmailAndRole(email, type))
            throw new UserException(UserErrorCode.DUPLICATED_USER);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);

        Role role = roleRepository.findByName(type).orElseThrow(()-> new UserException(UserErrorCode.INVALID_ROLE));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        userRoleRepository.save(userRole);

        Profile profile = Profile.builder()
                .profileImageUrl("")
                .user(user)
                .name(name)
                .uuid(UUID.randomUUID())
                .build();

        profileRepository.save(profile);

        user.setProfile(profile);
        user.updateUserRole(userRole);
        userRepository.save(user);

        return UserDto
                .builder()
                .user(user)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean existsUserByEmail(String email)
    {
        return userRepository.existsUserByEmail(email);
    }

    @Transactional
    public void changePassword(Authentication authentication, UUID userId, String newPassword)
    {
        if(authentication == null)
            throw new UserException(UserErrorCode.INVALID_DATA);

        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        if(details == null
            || details.getUserDto() == null)
            throw new UserException(UserErrorCode.INVALID_DATA);

        if(details.getUserDto().getId().equals(userId) == false)
            throw new UserException(UserErrorCode.INVALID_ROLE);

        User user = userRepository.findByUuid(userId).orElseThrow(()-> new UserException(UserErrorCode.INVALID_DATA));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        temporaryPasswordService.deleteFromUserByEmail(user.getEmail());
    }
}
