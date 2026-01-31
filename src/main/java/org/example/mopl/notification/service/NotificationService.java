package org.example.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.event.message.NotificationCreatedEvent;
import org.example.mopl.notification.dto.CursorResponseNotificationDto;
import org.example.mopl.notification.dto.NotificationDto;
import org.example.mopl.notification.dto.NotificationListRequest;
import org.example.mopl.notification.dto.NotificationSearchCondition;
import org.example.mopl.notification.enums.Level;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.enums.SortBy;
import org.example.mopl.notification.exception.NotificationForbiddenException;
import org.example.mopl.notification.exception.NotificationNotFoundException;
import org.example.mopl.notification.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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

        Notification notification = notificationRepository.findByUuid(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.isSameReceiverId(receiverId)) {
            throw new NotificationForbiddenException(receiverId);
        }
        notificationRepository.delete(notification);
        log.info("알림 삭제 완료: notificationId={}", notificationId);
    }

    @Transactional(readOnly = true)
    public CursorResponseNotificationDto findAll(UUID receiverId, NotificationListRequest request) {

        Long idAfter = notificationRepository.findIdByUuid(request.idAfter()).orElse(null);

        List<Notification> notifications = notificationRepository.searchByCursor(
                NotificationSearchCondition.builder().receiverId(receiverId).cursor(request.cursor())
                        .idAfter(idAfter).limit(request.limit()+1).sortDirection(request.sortDirection())
                        .sortBy(request.sortBy()).build());

        Long totalCount = notificationRepository.countByReceiverId(receiverId);

        CursorResponseNotificationDto result = CursorResponseNotificationDto.of(
                notifications, request.limit(), totalCount, request.sortBy(), request.sortDirection()
        );

        log.info("알림 목록 조회 완료: receiverId={}", receiverId);
        return result;
    }
}
