package org.example.mopl.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.notification.dto.CursorResponseNotificationDto;
import org.example.mopl.notification.dto.NotificationListRequest;
import org.example.mopl.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorResponseNotificationDto> findAllByReceiverId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute NotificationListRequest request
            ) {
        UUID receiverId = userDetails.getUserDto().getId();
        log.info("알림 목록 조회 요청: receiverId={}", receiverId);

        CursorResponseNotificationDto result = notificationService.findAll(receiverId, request);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping(path = "/{notificationId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID notificationId) {
        UUID receiverId = userDetails.getUserDto().getId();
        log.info("알림 삭제 요청: id={}, receiverId={}", notificationId, receiverId);

        notificationService.delete(notificationId, receiverId);
        return ResponseEntity.noContent().build();
    }
}
