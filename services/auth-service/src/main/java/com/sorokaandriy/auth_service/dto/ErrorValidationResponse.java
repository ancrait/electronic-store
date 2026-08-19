package com.sorokaandriy.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorValidationResponse {

    private int status;
    private String message;
    private Map<String, String> errors;
    private Long timestamp;

    public ErrorValidationResponse(Map<String, String> errors) {
        this.status = 400;
        this.message = "Validation failed";
        this.errors = errors;
        this.timestamp = System.currentTimeMillis();
    }
}
