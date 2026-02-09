package org.example.mopl.watchtogether.controller;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WatchTogetherWebSocketController {

    private final WatchTogetherService watchTogetherService;

    @MessageMapping("contents/{contentId}/chat")
    public void sendMessage(@DestinationVariable String contentId,
                            @Payload ContentChatSendRequest chatSendRequest,
                            Principal principal
    ){
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        watchTogetherService.sendMessageToRoom(contentId, chatSendRequest,new Watcher(userDetails.getUserDto()));
    }

}
