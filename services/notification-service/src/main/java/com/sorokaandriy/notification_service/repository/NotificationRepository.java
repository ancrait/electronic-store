package com.sorokaandriy.notification_service.repository;

import com.sorokaandriy.notification_service.model.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findAllByRecipient(String recipient, Pageable pageable);
}
