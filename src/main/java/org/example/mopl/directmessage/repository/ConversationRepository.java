package org.example.mopl.directmessage.repository;

import org.example.mopl.directmessage.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long>, ConversationRepositoryCustom {

    Optional<Conversation> findByUuid (UUID conversationUuid);

    List<Conversation> findByUuidIn(List<UUID> conversationUuids);
}
