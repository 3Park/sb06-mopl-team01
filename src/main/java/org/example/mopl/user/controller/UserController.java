package org.example.mopl.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mopl.user.dto.CursorResponseUserDto;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.dto.request.*;
import org.example.mopl.user.enums.UserRoleType;
import org.example.mopl.user.enums.UserSortBy;
import org.example.mopl.user.enums.UserSortDirection;
import org.example.mopl.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursorResponseUserDto> getAllUsers(@ModelAttribute UserCursorRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUsers(request));
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId, @RequestBody @Valid ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userService.changePassword(authentication, userId, request.password());
        return ResponseEntity.ok().build();
    }

    /* 사용자 상세 조회 = 프로필 조회 → ProfileController GET /api/users/{userId} (ProfileDto) 로 통일 */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @RequestBody @Valid ChangeRoleRequest request) {
        userService.changeRole(userId, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/locked")
    public ResponseEntity<Void> updateUserLockStatus(@PathVariable UUID userId, @RequestBody @Valid ChangeUserLockStatus request) {
        userService.changeLockStatus(userId, request);
        return ResponseEntity.ok().build();
    }
}
