package com.ia.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The single error envelope returned by every failure path: MVC handlers, bean validation
 * and the security filter chains. Clients can therefore rely on one shape:
 *
 * <pre>
 * {
 *   "timestamp": "2026-09-17T12:00:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/auth/register",
 *   "fieldErrors": { "password": "Password is too weak." }
 * }
 * </pre>
 *
 * <p>{@code fieldErrors} is omitted when empty.</p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, Map.of());
    }

    public static ApiErrorResponse of(HttpStatus status, String message, String path,
            Map<String, String> fieldErrors) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors == null ? Map.of() : fieldErrors
        );
    }
}
