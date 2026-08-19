package com.sorokaandriy.notification_service.dto;

import com.sorokaandriy.notification_service.model.NotificationStatus;
import com.sorokaandriy.notification_service.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String recipient;
    private String subject;
    private NotificationType type;
    private NotificationStatus status;
    private String errorMessage;
    private String relatedEntityId;
    private Long createdAt;
}
