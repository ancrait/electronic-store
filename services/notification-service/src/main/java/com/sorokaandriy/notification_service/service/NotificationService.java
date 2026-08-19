package com.sorokaandriy.notification_service.service;

import com.sorokaandriy.notification_service.dto.NotificationResponse;
import com.sorokaandriy.notification_service.kafka.OrderCreatedEvent;
import com.sorokaandriy.notification_service.kafka.UserRegisteredEvent;
import com.sorokaandriy.notification_service.model.NotificationEntity;
import com.sorokaandriy.notification_service.model.NotificationStatus;
import com.sorokaandriy.notification_service.model.NotificationType;
import com.sorokaandriy.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final EmailTemplateBuilder templateBuilder;
    private final NotificationRepository repository;

    public NotificationService(EmailService emailService,
                               EmailTemplateBuilder templateBuilder,
                               NotificationRepository repository) {
        this.emailService = emailService;
        this.templateBuilder = templateBuilder;
        this.repository = repository;
    }

    public void sendRegistrationConfirmation(UserRegisteredEvent event) {
        String subject = "Ласкаво просимо до VOLT";
        send(event.getEmail(),
                subject,
                templateBuilder.buildRegistrationEmail(event),
                NotificationType.REGISTRATION_CONFIRMATION,
                event.getUserId());
    }

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        String subject = "Замовлення №" + event.getOrderId() + " підтверджено";
        send(event.getEmail(),
                subject,
                templateBuilder.buildOrderEmail(event),
                NotificationType.ORDER_CONFIRMATION,
                String.valueOf(event.getOrderId()));
    }

    public Page<NotificationResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(pageable).map(this::toResponse);
    }

    private void send(String recipient,
                      String subject,
                      String body,
                      NotificationType type,
                      String relatedEntityId) {

        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .subject(subject)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .status(NotificationStatus.SENT)
                .build();

        try {
            emailService.sendHtmlEmail(recipient, subject, body);
            log.info("Email '{}' sent to {}", subject, recipient);
        } catch (Exception exception) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(exception.getMessage());
            log.error("Cannot send email to {}: {}", recipient, exception.getMessage());
        }

        repository.save(notification);
    }

    private NotificationResponse toResponse(NotificationEntity notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .type(notification.getType())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .relatedEntityId(notification.getRelatedEntityId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
