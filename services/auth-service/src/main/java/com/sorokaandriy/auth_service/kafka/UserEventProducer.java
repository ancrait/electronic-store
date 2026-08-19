package com.sorokaandriy.auth_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String userRegisteredTopic;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${kafka.topic.user-registered}") String userRegisteredTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRegisteredTopic = userRegisteredTopic;
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Sending UserRegisteredEvent for user {}", event.getUserId());
        kafkaTemplate.send(userRegisteredTopic, event.getUserId(), event);
    }
}
