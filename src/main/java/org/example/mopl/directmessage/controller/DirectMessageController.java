package org.example.mopl.directmessage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.directmessage.dto.request.ConversationCreateRequest;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.dto.request.ConversationListRequest;
import org.example.mopl.directmessage.dto.request.DirectMessageListRequest;
import org.example.mopl.directmessage.dto.response.CursorResponseConversationDto;
import org.example.mopl.directmessage.dto.response.CursorResponseDirectMessageDto;
import org.example.mopl.directmessage.service.DirectMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conversations")
public class DirectMessageController {
    private final DirectMessageService directMessageService;

    // 대화 생성
    @PostMapping
    public ResponseEntity<ConversationDto> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ConversationCreateRequest request
            ) {
        log.info("conversation 생성 요청, userId={}", request.withUserId());
        ConversationDto conversationDto = directMessageService
                .create(userDetails.getUserDto().getId(), request.withUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationDto);
    }

    // DM 읽음 처리
    @PostMapping(path = "/{conversationId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @PathVariable UUID directMessageId
            ) {
        log.info("DM 읽음 요청, conversationId={}, directMessageId={}", conversationId, directMessageId);
        directMessageService.read(conversationId, directMessageId, userDetails.getUserDto().getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 대화 조회
    @GetMapping(path = "/{conversationId}")
    public ResponseEntity<ConversationDto> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId
    ) {
        log.info("대화 조회 요청, conversationId={}", conversationId);
        ConversationDto conversationDto = directMessageService.get(
                userDetails.getUserDto().getId(), conversationId
        );
        return ResponseEntity.status(HttpStatus.OK).body(conversationDto);
    }

    // 특정 사용자와의 대화 조회
    @GetMapping(path = "/with")
    public ResponseEntity<ConversationDto> getWith(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID userId
    ) {
        log.info("with={} 사용자와 대화 조회 요청", userId);
        ConversationDto conversationDto = directMessageService.getWith(
                userDetails.getUserDto().getId(), userId
        );
        return ResponseEntity.status(HttpStatus.OK).body(conversationDto);
    }

    // 대화 목록 조회
    @GetMapping
    public ResponseEntity<CursorResponseConversationDto> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute @Valid ConversationListRequest request
            ) {
        log.info("대화 목록 조회 요청, requesterId={}", userDetails.getUserDto().getId());
        CursorResponseConversationDto result = directMessageService.getConversations(
                userDetails.getUserDto().getId(), request
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // DB 목록 조회
    @GetMapping(path = "{conversationId}/direct-messages")
    public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @ModelAttribute @Valid DirectMessageListRequest request
    ) {
        log.info("DM 목록 조회 요청, requesterId={}", userDetails.getUserDto().getId());
        CursorResponseDirectMessageDto result = directMessageService.getDirectMessages(
                userDetails.getUserDto().getId(), conversationId, request
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
