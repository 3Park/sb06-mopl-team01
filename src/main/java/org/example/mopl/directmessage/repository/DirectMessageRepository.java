package org.example.mopl.directmessage.repository;

import org.example.mopl.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long>, DirectMessageRepositoryCustom {
    Optional<DirectMessage> findFirstByConversationIdOrderByIdDesc(Long conversationId);

    Optional<DirectMessage> findByUuid(UUID directMEssageUuid);

}
