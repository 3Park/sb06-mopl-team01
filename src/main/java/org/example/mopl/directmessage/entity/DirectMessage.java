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
@Table(name = "direct_messages")
public class DirectMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false, updatable = false)
    private Conversation conversation;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false, updatable = false)
    private Long receiverId;

    @Column(nullable = false, updatable = false)
    private String content;

    @Column(name = "read_status", nullable = false)
    private boolean isRead;

    public static DirectMessage of(Conversation conversation, Long senderId, Long receiverId, String content) {
        return DirectMessage.builder()
                .isRead(false)
                .conversation(conversation)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .build();
    }

    public boolean isSenderId(Long userId) {
        return this.senderId.equals(userId);
    }
    public boolean isReceiverId(Long userId) {
        return this.receiverId.equals(userId);
    }
    public boolean isValidParticipant(Long userId) {
        return isSenderId(userId) || isReceiverId(userId);
    }
    public boolean isValidParticipants(Long userId, Long otherUserId) {
        return isValidParticipant(userId) || isValidParticipant(otherUserId);
    }

    public boolean isUnreadBy(Long userId) {
        return !this.isRead && this.receiverId.equals(userId);
    }
    public void read() {
        this.isRead = true;
    }
}
