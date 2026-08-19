package com.sorokaandriy.order_service.security;

public record AuthenticatedUser(String id, String email, String role) {
}
