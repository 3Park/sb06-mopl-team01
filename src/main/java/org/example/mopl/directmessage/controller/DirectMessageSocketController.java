package org.example.mopl.directmessage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.directmessage.dto.request.DirectMessageCreateRequest;
import org.example.mopl.directmessage.service.DirectMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageSocketController {

    private final DirectMessageService directMessageService;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    public void sendDirectMessages(
            @DestinationVariable("conversationId") UUID conversationUuid,
            @Payload @Valid DirectMessageCreateRequest request,
            Principal principal) {
        log.info("directmessage 전송, conversationId={}", conversationUuid);
        if (principal == null) { log.error("인증 정보가 유실되었습니다."); return;}

        Authentication authentication = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UUID senderUuid = userDetails.getUserDto().getId();
        directMessageService.saveAndSendMessage(conversationUuid, senderUuid, request.content());
    }
}
