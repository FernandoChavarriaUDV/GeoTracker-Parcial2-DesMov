package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandardResponse<T>(int httpCode, String message, T body, Instant timestamp) {
    public static <T> StandardResponse<T> of(int code, String message, T body) {
        return new StandardResponse<>(code, message, body, Instant.now());
    }
}

