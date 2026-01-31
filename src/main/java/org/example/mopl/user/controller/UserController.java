package org.example.mopl.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.dto.request.UserCreateRequest;
import org.example.mopl.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }
}
