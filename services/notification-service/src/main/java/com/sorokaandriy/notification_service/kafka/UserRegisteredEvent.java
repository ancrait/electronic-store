package com.sorokaandriy.notification_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Long registeredAt;
}
