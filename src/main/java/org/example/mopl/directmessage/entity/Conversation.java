package org.example.mopl.directmessage.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "direct_conversations")
public class Conversation {

    @Id
    @Tsid
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "creator_id", nullable = false, updatable = false)
    private Long creatorId;

    @Column(name = "join_id", nullable = false, updatable = false)
    private Long joinId;


    @Builder
    private Conversation(Long creatorId, Long joinId) {
        this.uuid = UUID.randomUUID();

        this.creatorId = creatorId;
        this.joinId = joinId;
    }

    public static Conversation of(Long creatorId, Long joinId) {
        return Conversation.builder().creatorId(creatorId).joinId(joinId).build();
    }
    public boolean isValidParticipant(Long userId) {
        return creatorId.equals(userId) || joinId.equals(userId);
    }
    public Long getCounterpartId(Long senderId) {
        return (creatorId.equals(senderId))? joinId : creatorId;
    }
}
