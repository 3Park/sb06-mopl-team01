package org.example.mopl.directmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.directmessage.dto.DirectMessageDto;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.exception.ConversationNotFoundException;
import org.example.mopl.directmessage.exception.ParticipantNotFoundException;
import org.example.mopl.directmessage.repository.ConversationRepository;
import org.example.mopl.directmessage.repository.DirectMessageRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void saveAndSendMessage(UUID conversationUuid, UUID senderUuid, String content) {

        Conversation conversation = conversationRepository.findByUuid(conversationUuid)
                .orElseThrow(() -> new ConversationNotFoundException(conversationUuid));

        User sender = userRepository.findUserAndProfileOnlyByUuid(senderUuid)
                .orElseThrow(() -> new ParticipantNotFoundException(senderUuid));
        Long receiverId = conversation.findCounterpartId(sender.getId());
        User receiver = userRepository.findUserAndProfileOnlyById(receiverId)
                .orElseThrow(() -> new ParticipantNotFoundException());

        DirectMessage directMessage = DirectMessage.of(conversation, sender.getId(), receiver.getId(), content);
        directMessageRepository.save(directMessage);
        log.debug("메시지 저장 완료, messageId={}", directMessage.getUuid());

        DirectMessageDto dto = DirectMessageDto.from(directMessage, sender, receiver);
        messagingTemplate.convertAndSend(resolveDestination(conversationUuid), dto);

        log.info("메시지 전송 완료, messageId={}", directMessage.getUuid());
    }


    private String resolveDestination(UUID conversationUuid) {
        return "/sub/conversations/" + conversationUuid + "/direct-messages";
    }
}
