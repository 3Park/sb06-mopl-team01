package org.example.mopl.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.event.message.*;
import org.example.mopl.notification.enums.Level;
import org.example.mopl.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = UserRoleUpdatedKafkaEvent.TOPIC)
    public void onUserRoleUpdatedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - 유저 권한 변경");
        try {
            UserRoleUpdatedKafkaEvent event = objectMapper.readValue(kafkaEvent, UserRoleUpdatedKafkaEvent.class);

            UUID receiverId = event.receiverId();
            String title = "내 권한이 변경되었어요.";
            String content = "내 권한이 [" + event.beforeRole() + "]에서 [" +  event.afterRole() + "]로 변경되었어요.";
            Level level = Level.INFO;

            notificationService.create(receiverId, title, content, level);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = UserFollowCreatedKafkaEvent.TOPIC)
    public void onUserFollowCreatedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - 팔로우 발생");
        try {
            UserFollowCreatedKafkaEvent event = objectMapper.readValue(kafkaEvent, UserFollowCreatedKafkaEvent.class);

            UUID receiverId = event.receiverId();
            String title = event.followerName() + "님이 나를 팔로우했어요.";
            String content = null;
            Level level = Level.INFO;

            notificationService.create(receiverId, title, content, level);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = PlaylistSubscriptionCreatedKafkaEvent.TOPIC)
    public void onPlaylistSubscriptionCreatedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - 플레이리스트 구독 발생");
        try {
            PlaylistSubscriptionCreatedKafkaEvent event = objectMapper.readValue(kafkaEvent, PlaylistSubscriptionCreatedKafkaEvent.class);

            UUID receiverId = event.receiverId();
            String title = event.subscriberName() + "님이 내 플레이리스트를 구독했어요.";
            String content = "[" + event.playlistTitle() + "] " + event.playlistDescription();
            Level level = Level.INFO;

            notificationService.create(receiverId, title, content, level);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = PlaylistCreatedKafkaEvent.TOPIC)
    public void onPlaylistCreatedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - 플레이리스트 생성");
        try {
            PlaylistCreatedKafkaEvent event = objectMapper.readValue(kafkaEvent, PlaylistCreatedKafkaEvent.class);

            List<UUID> receiverIds = event.receiverIds();
            if (receiverIds == null || receiverIds.isEmpty()) {
                return;
            }

            String title = event.creatorName() + "님이 플레이리스트를 만들었어요.";
            String content = "[" + event.playlistTitle() + "] " + event.playlistDescription();
            Level level = Level.INFO;

            for (UUID receiverId : receiverIds) {
                notificationService.create(receiverId, title, content, level);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = PlaylistContentAddedKafkaEvent.TOPIC)
    public void onPlaylistContentAddedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - 플레이리스트에 새 컨텐츠 추가");
        try {
            PlaylistContentAddedKafkaEvent event = objectMapper.readValue(kafkaEvent, PlaylistContentAddedKafkaEvent.class);

            UUID receiverId = event.receiverId();
            String title = "새 컨텐츠가 추가되었어요.";
            String content = "[" + event.playlistTitle() + "]에 " + event.contentTitle() + "이(가) 추가되었어요.";
            Level level = Level.INFO;

            notificationService.create(receiverId, title, content, level);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DmMessageReceivedKafkaEvent.TOPIC)
    public void onDmMessageReceivedEvent(String kafkaEvent) {
        log.debug("kafka 이벤트 수신 - DM 수신");
        try {
            DmMessageReceivedKafkaEvent event = objectMapper.readValue(kafkaEvent, DmMessageReceivedKafkaEvent.class);

            UUID receiverId = event.receiverId();
            String title = "[DM] " + event.senderName();
            String content = event.message();
            Level level = Level.INFO;

            notificationService.create(receiverId, title, content, level);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
