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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void create(UUID receiverId, String title, String content, Level level) {

        Notification notification = Notification.of(receiverId, title, content, level);
        notification = notificationRepository.save(notification);

        NotificationDto notificationDto = NotificationDto.from(notification);
        applicationEventPublisher.publishEvent(NotificationCreatedEvent.of(notificationDto));

        log.info("새 알림 생성 완료: receiverId={}", receiverId);
    }

    @Transactional
    public void delete(UUID notificationId, UUID receiverId) {

        Notification notification = getNotification(notificationId);

        validateOwnership(notification, receiverId);

        notificationRepository.delete(notification);
        log.info("알림 삭제 완료: notificationId={}", notificationId);
    }

    @Transactional(readOnly = true)
    public CursorResponseNotificationDto findAll(UUID receiverId, NotificationListRequest request) {

        Long idAfter = (request.idAfter() != null) ?
                notificationRepository.findIdByUuid(request.idAfter()).orElse(null)
                : null;

        NotificationSearchCondition condition = request.toSearchCondition(
                receiverId, request.limit()+1, idAfter
        );

        List<Notification> notifications = notificationRepository.searchByCursor(condition);

        Long totalCount = notificationRepository.countByReceiverId(receiverId);

        CursorResult cursorResult = applyCursorAndTrim(notifications, request.limit());

        List<NotificationDto> data = notifications.stream().map(NotificationDto::from).toList();


        log.info("알림 목록 조회 완료: receiverId={}", receiverId);
        return CursorResponseNotificationDto.builder()
                .data(data)
                .nextCursor(cursorResult.nextCursor()).nextIdAfter(cursorResult.nextIdAfter())
                .hasNext(cursorResult.hasNext()).totalCount(totalCount)
                .sortBy(request.sortBy()).sortDirection(request.sortDirection())
                .build();
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

    private CursorResult applyCursorAndTrim (List<Notification> notifications, int limit) {
        boolean hasNext = false;
        String nextCursor = null;
        UUID nextIdAfter = null;

        if (notifications.size() > limit) {
            hasNext = true;
            notifications.remove(notifications.size() - 1);
        }
        if (!notifications.isEmpty()) {
            nextIdAfter = notifications.get(notifications.size() - 1).getUuid();
            nextCursor = nextIdAfter.toString();
        }
        return new CursorResult(hasNext, nextCursor, nextIdAfter);
    }
}
