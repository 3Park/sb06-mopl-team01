package org.example.mopl.directmessage.repository;

import org.example.mopl.directmessage.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT COUNT(c) > 0 " +
            "FROM User u " +
            "JOIN Conversation c ON (c.creatorId = u.id OR c.joinId = u.id) " +
            "WHERE u.uuid = :userUuid " +
            "AND c.uuid = :conversationUuid")
    boolean existsByConversationUuidAndUserUuid(
            @Param("conversationUuid") UUID conversationId,
            @Param("userUuid") UUID userId
    );

    Optional<Conversation> findByUuid (UUID conversationUuid);
}
