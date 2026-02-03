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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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



}
