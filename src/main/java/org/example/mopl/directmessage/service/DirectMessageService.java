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

        Conversation conversation = getConversationOrThrow(conversationUuid);
        User sender = getSenderOrThrow(senderUuid);

        User receiver = getCounterpartOrThrow(conversation, sender.getId());

        DirectMessage directMessage = saveMessage(conversation, sender, receiver, content);
        log.debug("메시지 저장 완료, messageId={}", directMessage.getUuid());

        sendToSocket(conversationUuid, directMessage, sender, receiver);
        log.info("메시지 전송 완료, messageId={}", directMessage.getUuid());
    }




    private String resolveDestination(UUID conversationUuid) {
        return "/sub/conversations/" + conversationUuid + "/direct-messages";
    }


    private Conversation getConversationOrThrow(UUID conversationUuid) {
        return conversationRepository.findByUuid(conversationUuid)
                .orElseThrow(() -> new ConversationNotFoundException(conversationUuid));
    }

    private User getSenderOrThrow(UUID senderUuid) {
        return userRepository.findUserAndProfileOnlyByUuid(senderUuid)
                .orElseThrow(() -> new ParticipantNotFoundException(senderUuid));
    }

    private User getCounterpartOrThrow(Conversation conversation, Long senderId) {
        if(!conversation.isValidParticipant(senderId)) throw new ParticipantNotFoundException();
        Long receiverId = conversation.getCounterpartId(senderId);
        return userRepository.findUserAndProfileOnlyById(receiverId)
                .orElseThrow(ParticipantNotFoundException::new);
    }

    private DirectMessage saveMessage(Conversation conversation, User sender, User receiver, String content) {
        DirectMessage directMessage = DirectMessage.of(conversation, sender.getId(), receiver.getId(), content);
        return directMessageRepository.save(directMessage);
    }

    private void sendToSocket(UUID conversationUuid, DirectMessage directMessage, User sender, User receiver) {
        DirectMessageDto dto = DirectMessageDto.from(directMessage, sender, receiver);
        messagingTemplate.convertAndSend(resolveDestination(conversationUuid), dto);
    }
}
