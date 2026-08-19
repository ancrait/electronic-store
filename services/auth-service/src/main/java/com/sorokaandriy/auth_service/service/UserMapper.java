package com.sorokaandriy.auth_service.service;

import com.sorokaandriy.auth_service.dto.RegisterRequest;
import com.sorokaandriy.auth_service.dto.UserResponse;
import com.sorokaandriy.auth_service.model.Role;
import com.sorokaandriy.auth_service.model.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserEntity fromRegisterRequestToUser(RegisterRequest request, String encodedPassword) {
        return UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(encodedPassword)
                .phone(request.getPhone())
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    public UserResponse fromUserToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
