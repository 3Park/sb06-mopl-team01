package org.example.mopl.directmessage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.directmessage.dto.ConversationCreateRequest;
import org.example.mopl.directmessage.dto.ConversationDto;
import org.example.mopl.directmessage.service.DirectMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController("/api/conversations")
public class DirectMessageController {
    private final DirectMessageService directMessageService;

    @PostMapping
    public ResponseEntity<ConversationDto> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ConversationCreateRequest request
            ) {
        log.info("conversation 생성, userId={}", request.withUserId());
        ConversationDto conversationDto = directMessageService
                .create(userDetails.getUserDto().getId(), request.withUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationDto);
    }

    @PostMapping(path = "/{conversationId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @PathVariable UUID directMessageId
            ) {
        log.info("DM 읽음, conversationId={}, directMessageId={}", conversationId, directMessageId);
        directMessageService.read(conversationId, directMessageId, userDetails.getUserDto().getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
