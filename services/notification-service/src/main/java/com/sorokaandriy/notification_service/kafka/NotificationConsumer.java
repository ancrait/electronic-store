package com.sorokaandriy.notification_service.kafka;

import com.sorokaandriy.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService service;

    public NotificationConsumer(NotificationService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "${kafka.topic.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userRegisteredListenerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for {}", event.getEmail());
        service.sendRegistrationConfirmation(event);
    }

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCreatedListenerFactory"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.getOrderId());
        service.sendOrderConfirmation(event);
    }
}
