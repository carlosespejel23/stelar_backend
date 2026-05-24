package com.education.stelar.kernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, null, Instant.now());
    }

    public ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
        this(code, message, fieldErrors, Instant.now());
    }
}
