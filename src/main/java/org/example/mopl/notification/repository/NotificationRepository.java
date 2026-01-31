package org.example.mopl.notification.repository;

import org.example.mopl.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
        NotificationRepositoryCustom {

    List<Notification> findAllByReceiverIdAndCreatedAtAfter(
            UUID receiverId, LocalDateTime createdAt, Pageable pageable
    );

    Optional<Notification> findByUuid(UUID notificationUuid);

    @Query("SELECT n.id FROM Notification n WHERE n.uuid = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") UUID uuid);

    long countByReceiverId(UUID receiverId);
}
