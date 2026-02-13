package org.example.mopl.notification;

import org.example.mopl.notification.dto.request.NotificationListRequest;
import org.example.mopl.notification.dto.response.CursorResponseNotificationDto;
import org.example.mopl.notification.entity.Notification;
import org.example.mopl.notification.enums.Level;
import org.example.mopl.notification.enums.SortBy;
import org.example.mopl.notification.exception.NotificationForbiddenException;
import org.example.mopl.notification.repository.NotificationRepository;
import org.example.mopl.notification.service.NotificationService;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test-dm")
@Testcontainers
public class NotificationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;
    @MockitoBean
    private ApplicationEventPublisher eventPublisher;
    @MockitoBean(name = "commandLineRunner")
    private CommandLineRunner commandLineRunner;


    UUID receiverId;
    String title;
    String content;
    Level level;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        title = "알림명";
        content = "알림 내용";
        level = Level.INFO;
    }

    @Test
    @DisplayName("알림 생성 성공")
    void create_Success() {
        // when
        notificationService.create(receiverId, title, content, level);

        // then
        List<Notification> notifications = notificationRepository.findAll();

        assertThat(notifications.size()).isEqualTo(1);
        assertThat(notifications.get(0).getReceiverId()).isEqualTo(receiverId);
        assertThat(notifications.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void delete_Success() {
        // given
        Notification targetNotification = createNotification(receiverId, title, content, level);

        // when
        notificationService.delete(targetNotification.getUuid(), receiverId);

        // then
        assertThat(notificationRepository.findAll()).isEmpty();
    }
    @Test
    @DisplayName("알림 삭제 실패 - 권한 없음")
    void delete_NotificationForbidden_Fail() {
        // given
        Notification targetNotification = createNotification(receiverId, title, content, level);
        UUID invalidUserId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> notificationService.delete(targetNotification.getUuid(), invalidUserId))
                .isInstanceOf(NotificationForbiddenException.class);
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 첫 페이지")
    void getNotifications_FirstPage_Success() {
        // given
        Notification firstNotification = createNotification(receiverId, title, content, level);
        Notification secondNotification = createNotification(receiverId, title, content, level);
        Notification notRelatedNotification = createNotification(UUID.randomUUID(), title, content, level);

        // when
        NotificationListRequest request = NotificationListRequest.builder()
                .limit(1).sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseNotificationDto result = notificationService.getNotifications(receiverId, request);

        // then
        assertThat(result.data().size()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextIdAfter()).isEqualTo(firstNotification.getUuid());
    }
    @Test
    @DisplayName("알림 목록 조회 성공 - 페이지네이션")
    void getNotifications_Pagination_Success() {
        // given
        Notification firstNotification = createNotification(receiverId, title, content, level);
        Notification secondNotification = createNotification(receiverId, title, content, level);
        Notification notRelatedNotification = createNotification(UUID.randomUUID(), title, content, level);

        // when
        NotificationListRequest request = NotificationListRequest.builder()
                .limit(1).idAfter(firstNotification.getUuid())
                .sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseNotificationDto result = notificationService.getNotifications(receiverId, request);

        // then
        assertThat(result.data().size()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextIdAfter()).isEqualTo(secondNotification.getUuid());
    }


    // === helper method ===
    private Notification createNotification(UUID receiverId, String title, String content, Level level) {
        return notificationRepository.save(Notification.of(receiverId, title, content, level));
    }
}
