package com.sorokaandriy.order_service.exception;

public class AccessDeniedForOrderException extends RuntimeException {

    public AccessDeniedForOrderException(String message) {
        super(message);
    }
}
