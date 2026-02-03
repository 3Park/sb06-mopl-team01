package org.example.mopl.directmessage.entity;

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
@Table(name = "direct_messages")
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false) // 이거 따로 지정할 필요 없나?
    private LocalDateTime createdAt;


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
    private boolean readStatus;


    @Builder
    private DirectMessage(Conversation conversation, Long senderId, Long receiverId, String content) {
        this.uuid = UUID.randomUUID();
        this.readStatus = false;

        this.conversation = conversation;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public static DirectMessage of(Conversation conversation, Long senderId, Long receiverId, String content) {
        return DirectMessage.builder().conversation(conversation).senderId(senderId)
                .receiverId(receiverId).content(content).build();
    }

}
