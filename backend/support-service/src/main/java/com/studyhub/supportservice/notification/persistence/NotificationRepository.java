package com.studyhub.supportservice.notification.persistence;

import com.studyhub.supportservice.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(String userId, boolean read);

    Optional<Notification> findByIdAndUserId(Long id, String userId);

    long countByUserIdAndReadFalse(String userId);
}
