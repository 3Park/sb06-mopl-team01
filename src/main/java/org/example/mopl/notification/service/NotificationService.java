package org.example.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.event.message.NotificationCreatedEvent;
import org.example.mopl.notification.dto.CursorResult;
import org.example.mopl.notification.dto.response.CursorResponseNotificationDto;
import org.example.mopl.notification.dto.data.NotificationDto;
import org.example.mopl.notification.dto.request.NotificationListRequest;
import org.example.mopl.notification.dto.NotificationSearchCondition;
import org.example.mopl.notification.enums.Level;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.exception.NotificationForbiddenException;
import org.example.mopl.notification.exception.NotificationNotFoundException;
import org.example.mopl.notification.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void create(UUID receiverId, String title, String content, Level level) {

        Notification notification = Notification.of(receiverId, title, content, level);
        notification = notificationRepository.save(notification);
        increaseCount(receiverId);

        NotificationDto notificationDto = NotificationDto.from(notification);
        applicationEventPublisher.publishEvent(NotificationCreatedEvent.of(notificationDto));

        log.info("새 알림 생성 완료: receiverId={}", receiverId);
    }

    @Transactional
    public void delete(UUID notificationId, UUID receiverId) {

        Notification notification = getNotification(notificationId);

        validateOwnership(notification, receiverId);

        notificationRepository.delete(notification);
        decreaseCount(receiverId);

        log.info("알림 삭제 완료: notificationId={}", notificationId);
    }

    @Transactional(readOnly = true)
    public CursorResponseNotificationDto getNotifications(UUID receiverId, NotificationListRequest request) {

        Long idAfter = Optional.ofNullable(request.idAfter())
                .flatMap(notificationRepository::findIdByUuid)
                .orElse(null);

        NotificationSearchCondition condition = request.toSearchCondition(
                receiverId, request.limit()+1, idAfter
        );

        List<Notification> notifications = notificationRepository.searchByCursor(condition);

        Long totalCount = getUnreadCount(receiverId);

        CursorResult cursorResult = getCursorResult(notifications, request.limit());

        List<NotificationDto> data = cursorResult.notifications().stream()
                .map(NotificationDto::from).toList();


        log.info("알림 목록 조회 완료: receiverId={}", receiverId);
        return CursorResponseNotificationDto.builder()
                .data(data)
                .nextCursor(cursorResult.nextCursor()).nextIdAfter(cursorResult.nextIdAfter())
                .hasNext(cursorResult.hasNext()).totalCount(totalCount)
                .sortBy(request.sortBy()).sortDirection(request.sortDirection())
                .build();
    }

    private Long getUnreadCount(UUID userId) {
        String key = "notification:count:" + userId;

        String cacheCount = redisTemplate.opsForValue().get(key);

        if (cacheCount != null) {
            return Long.parseLong(cacheCount);
        }

        Long dbCount = notificationRepository.countByReceiverId(userId);
        redisTemplate.opsForValue().set(key, dbCount.toString(), 1, TimeUnit.HOURS);
        return dbCount;
    }

    private void increaseCount(UUID receiverId) {
        String key = "notification:count:" + receiverId;
        redisTemplate.opsForValue().increment(key);
    }

    private void decreaseCount(UUID receiverId) {
        String key = "notification:count:" + receiverId;
        redisTemplate.opsForValue().decrement(key);
    }

    private Notification getNotification(UUID notificationId) {
        return notificationRepository.findByUuid(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    private void validateOwnership(Notification notification, UUID receiverId) {
        if (!notification.isSameReceiverId(receiverId)) {
            throw new NotificationForbiddenException(receiverId);
        }
    }

    private CursorResult getCursorResult (List<Notification> notifications, int limit) {
        boolean hasNext = false;
        String nextCursor = null;
        UUID nextIdAfter = null;
        List<Notification> notificationsAfter = notifications;

        if (notifications.size() > limit) {
            hasNext = true;
            notificationsAfter = notifications.subList(0, limit);
        }
        if (!notificationsAfter.isEmpty()) {
            nextIdAfter = notifications.get(notificationsAfter.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }
        return new CursorResult(notificationsAfter, hasNext, nextCursor, nextIdAfter);
    }
}
