package org.example.mopl.profile.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.profile.dto.FollowDto;
import org.example.mopl.profile.dto.FollowRequest;
import org.example.mopl.profile.service.FollowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /* JWT 인증 사용자 UUID (없으면 null) */
    private static UUID currentUserUuidOrNull(CustomUserDetails userDetails) {
        return userDetails != null && userDetails.getUserDto() != null
                ? userDetails.getUserDto().getId()
                : null;
    }

    @PostMapping
    public ResponseEntity<FollowDto> create(
            @RequestBody @Valid FollowRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FollowDto dto = followService.create(currentUserUuidOrNull(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID followId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        followService.deleteByUuid(followId, currentUserUuidOrNull(userDetails));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followed-by-me")
    public ResponseEntity<Boolean> isFollowedByMe(
            @RequestParam("followeeId") UUID followeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean followed = followService.isFollowedByMe(
                currentUserUuidOrNull(userDetails), followeeId).isFollowed();
        return ResponseEntity.ok(followed);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getFollowerCount(@RequestParam("followeeId") UUID followeeId) {
        long count = followService.getFollowerCount(followeeId).getCount();
        return ResponseEntity.ok(count);
    }

}
