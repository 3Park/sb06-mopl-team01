package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.event.message.UserRoleUpdatedKafkaEvent;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.dto.CursorResponseUserDto;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.dto.request.ChangeRoleRequest;
import org.example.mopl.user.dto.request.ChangeUserLockStatus;
import org.example.mopl.user.dto.request.UserCreateRequest;
import org.example.mopl.user.dto.request.UserCursorRequest;
import org.example.mopl.user.entity.Role;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.entity.UserRole;
import org.example.mopl.user.enums.UserRoleType;
import org.example.mopl.user.event.UserRoleLockStatusChangedEvent;
import org.example.mopl.user.exception.UserErrorCode;
import org.example.mopl.user.exception.UserException;
import org.example.mopl.user.repository.RoleRepository;
import org.example.mopl.user.repository.UserRepository;
import org.example.mopl.user.repository.UserRoleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher applicationEventPublisher;

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

    @Transactional(readOnly = true)
    public CursorResponseUserDto getAllUsers(UserCursorRequest request)
    {
        if(request == null)
            throw new UserException(UserErrorCode.INVALID_DATA);

        //QueryDsl 메서드 호출
        List<User> users = userRepository.findAllUsers(request);
        boolean hasNext = false;
        String nextCursor = null;
        UUID idAfter = null;
        //JpaRepository 기본 메서드 count 호출
        Long totalCount = userRepository.count();

        if(users.size() > request.limit())
        {
            hasNext = true;
            users.remove(users.size() - 1);
            idAfter = users.get(users.size() - 1).getUuid();
            nextCursor = getNextCursor(request, users);
        }

        //userroles n+1 해결을 위해 fetch join을 해오기 위한 부분
        List<UUID> ids = users.stream().map(User::getUuid).toList();
        if(ids.isEmpty())
            return new CursorResponseUserDto(
                    List.of(),
                    null,
                    null,
                    false,
                    totalCount,
                    request.sortBy().name(),
                    request.sortDirection().name());

        //fetch join 후 정렬이 깨짐
        List<User> fetchedUsers = userRepository.findUsersByIds(ids);

        //uuid를 key로 하는 userMap을 생성 -> 정렬된 리스트를 재구성할떄 사용
        Map<UUID, User> userMap = fetchedUsers.stream()
                .collect(Collectors.toMap(User::getUuid, u -> u));

        //ids 는 paging 조건에 맞는 정렬형태. userMap에서 가져와 기존 정렬된 형태로 복구
        List<User> orderedUsers = ids.stream().map(userMap::get).toList();

        return CursorResponseUserDto.builder()
                .data(orderedUsers.stream()
                        .map(x -> UserDto.builder()
                                .user(x)
                                .build()).toList())
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .nextIdAfter(idAfter)
                .totalCount(totalCount)
                .sortDirection(request.sortDirection().name())
                .sortBy(request.sortBy().name())
                .build();

    }

    @Transactional(readOnly = true)
    public UserDto getDetailsUser(UUID userId)
    {
        User user = userRepository.findByUuid(userId).orElseThrow(()-> new UserException(UserErrorCode.INVALID_USER));
        return UserDto.builder().user(user).build();
    }

    @Transactional
    public void changeRole(UUID userId, ChangeRoleRequest request)
    {
        User user = userRepository.findByUuid(userId).orElseThrow(()-> new UserException(UserErrorCode.INVALID_USER));
        if(user.getUserRoles() == null ||  user.getUserRoles().isEmpty()
            || user.getUserRoles().get(0).getRole() == null)
            throw new UserException(UserErrorCode.INVALID_DATA);

        String beforeRole = user.getUserRoles().get(0).getRole().getName().name();

        Role role = roleRepository.findByName(request.getRole()).orElseThrow(()-> new UserException(UserErrorCode.INVALID_ROLE));
        user.getUserRoles().get(0).setRole(role);
        userRoleRepository.save(user.getUserRoles().get(0));
        userRepository.save(user);

        applicationEventPublisher.publishEvent(UserRoleLockStatusChangedEvent.builder()
                .userEmail(user.getEmail())
                .build());

        //알림 호출
        applicationEventPublisher.publishEvent(new UserRoleUpdatedKafkaEvent(user.getUuid(),beforeRole, role.getName().name()));
    }

    @Transactional
    public void changeLockStatus(UUID userId, ChangeUserLockStatus request)
    {
        User user = userRepository.findByUuid(userId).orElseThrow(()-> new UserException(UserErrorCode.INVALID_USER));
        if(user.getUserRoles() == null ||  user.getUserRoles().isEmpty())
            throw new UserException(UserErrorCode.INVALID_DATA);

        user.setLocked(request.getLocked());
        userRepository.save(user);

        applicationEventPublisher.publishEvent(UserRoleLockStatusChangedEvent.builder()
                .userEmail(user.getEmail())
                .build());
    }

    private String getNextCursor(UserCursorRequest request, List<User> users)
    {
        switch (request.sortBy())
        {
            case name:
                return users.get(users.size() - 1).getProfile().getName();
            case email:
                return users.get(users.size() - 1).getEmail();
            case role:
                return users.get(users.size() - 1).getUserRoles().get(0).getRole().getName().name();
            case isLocked:
                return users.get(users.size() - 1).getLocked().toString();
            case createdAt:
                return users.get(users.size() - 1).getCreatedAt().toString();
            default:
                return null;
        }
    }
}
