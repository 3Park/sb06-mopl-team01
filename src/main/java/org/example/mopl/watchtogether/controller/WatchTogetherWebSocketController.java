package org.example.mopl.watchtogether.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.jwt.TokenUtils;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WatchTogetherWebSocketController {

    private final WatchTogetherService watchTogetherService;
    private final TokenUtils tokenUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @MessageMapping("contents/{contentId}/chat")
    public void sendMessage(@DestinationVariable String contentId,
                            @Payload ContentChatSendRequest chatSendRequest,
                            HttpServletRequest req
    ){
        String token = tokenUtils.getTokenFromRequest(req);
        CustomUserDetails authentication = (CustomUserDetails) jwtTokenProvider.getAuthentication(token).getDetails();
        watchTogetherService.sendMessageToRoom(contentId, chatSendRequest,new Watcher(authentication.getUserDto()));
    }

}
