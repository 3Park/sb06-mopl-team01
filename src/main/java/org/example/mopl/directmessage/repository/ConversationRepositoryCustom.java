package org.example.mopl.directmessage.repository;

import org.example.mopl.directmessage.entity.Conversation;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryCustom {
    Optional<Conversation> findExisting(Long userId1, Long userId2);

    boolean existsByConversationUuidAndUserUuid(UUID conversationUuid, UUID userUuid);
}
