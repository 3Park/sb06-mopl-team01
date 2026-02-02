package org.example.mopl.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.dto.request.ChangePasswordRequest;
import org.example.mopl.user.dto.request.UserCreateRequest;
import org.example.mopl.user.service.UserService;
import org.springframework.http.ResponseEntity;
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

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId, @RequestBody @Valid ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userService.changePassword(authentication, userId, request.getPassword());
        return ResponseEntity.ok().build();
    }
}
