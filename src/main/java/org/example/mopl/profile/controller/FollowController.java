package org.example.mopl.profile.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.profile.dto.FollowCreateRequest;
import org.example.mopl.profile.dto.FollowResponse;
import org.example.mopl.profile.dto.FollowUserDto;
import org.example.mopl.profile.dto.FollowedByMeResponse;
import org.example.mopl.profile.dto.FollowerCountResponse;
import org.example.mopl.profile.service.FollowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<FollowResponse> create(
            @RequestBody @Valid FollowCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) {
        FollowResponse response = followService.create(currentUserUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{followUuid}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID followUuid,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) {
        followService.deleteByUuid(followUuid, currentUserUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followed-by-me")
    public ResponseEntity<FollowedByMeResponse> isFollowedByMe(
            @RequestParam UUID followeeUuid,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserUuid
    ) {
        FollowedByMeResponse response = followService.isFollowedByMe(currentUserUuid, followeeUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<FollowerCountResponse> getFollowerCount(@RequestParam UUID followeeUuid) {
        FollowerCountResponse response = followService.getFollowerCount(followeeUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/following")
    public ResponseEntity<List<FollowUserDto>> getFollowingList(@RequestParam UUID userUuid) {
        List<FollowUserDto> list = followService.getFollowingList(userUuid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/followers")
    public ResponseEntity<List<FollowUserDto>> getFollowerList(@RequestParam UUID userUuid) {
        List<FollowUserDto> list = followService.getFollowerList(userUuid);
        return ResponseEntity.ok(list);
    }
}
