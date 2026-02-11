package org.example.mopl.directmessage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "direct_conversations")
public class Conversation extends BaseEntity {

    @Column(name = "creator_id", nullable = false, updatable = false)
    private Long creatorId;

    @Column(name = "join_id", nullable = false, updatable = false)
    private Long joinId;

    public static Conversation of(Long creatorId, Long joinId) {
        return Conversation.builder()
                .creatorId(creatorId).joinId(joinId)
                .build();
    }
    public boolean isValidParticipant(Long userId) {
        return creatorId.equals(userId) || joinId.equals(userId);
    }
    public Long getCounterpartId(Long senderId) {
        return (creatorId.equals(senderId))? joinId : creatorId;
    }
}
