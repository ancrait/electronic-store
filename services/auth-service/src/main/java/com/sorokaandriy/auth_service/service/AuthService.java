package com.sorokaandriy.auth_service.service;

import com.sorokaandriy.auth_service.dto.*;
import com.sorokaandriy.auth_service.exception.EmailAlreadyExistsException;
import com.sorokaandriy.auth_service.exception.InvalidCredentialsException;
import com.sorokaandriy.auth_service.exception.InvalidTokenException;
import com.sorokaandriy.auth_service.exception.UserNotFoundException;
import com.sorokaandriy.auth_service.kafka.UserEventProducer;
import com.sorokaandriy.auth_service.kafka.UserRegisteredEvent;
import com.sorokaandriy.auth_service.model.UserEntity;
import com.sorokaandriy.auth_service.repository.UserRepository;
import com.sorokaandriy.auth_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventProducer eventProducer;

    public AuthService(UserRepository repository,
                       UserMapper mapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserEventProducer eventProducer) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventProducer = eventProducer;
    }

    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        UserEntity user = mapper.fromRegisterRequestToUser(request, passwordEncoder.encode(request.getPassword()));
        UserEntity saved = repository.save(user);

        eventProducer.sendUserRegisteredEvent(UserRegisteredEvent.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .registeredAt(saved.getCreatedAt())
                .build());

        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = repository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.parseToken(request.getRefreshToken());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new InvalidTokenException("Provided token is not a refresh token");
        }

        UserEntity user = repository.findById(claims.getSubject())
                .orElseThrow(() -> new UserNotFoundException("User with id " + claims.getSubject() + " not found"));

        return buildAuthResponse(user);
    }

    public UserResponse getCurrentUser(String userId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        return mapper.fromUserToResponse(user);
    }

    public Page<UserResponse> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return repository.findAll(pageable).map(mapper::fromUserToResponse);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpiration())
                .user(mapper.fromUserToResponse(user))
                .build();
    }
}
