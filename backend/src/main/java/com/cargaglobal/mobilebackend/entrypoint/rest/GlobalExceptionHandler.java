package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.exception.EmailAlreadyExistsException;
import com.cargaglobal.mobilebackend.application.exception.InvalidCredentialsException;
import com.cargaglobal.mobilebackend.application.exception.UserNotFoundException;
import com.cargaglobal.mobilebackend.application.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(com.cargaglobal.mobilebackend.application.exception.LivenessUnavailableException.class)
    ResponseEntity<StandardResponse<Void>> livenessUnavailable(Exception ex) {
        return ResponseEntity.status(503).body(StandardResponse.of(503, ex.getMessage(), null));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ResponseEntity<StandardResponse<Void>> httpError(org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(StandardResponse.of(ex.getStatusCode().value(), ex.getReason(), null));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<StandardResponse<Void>> forbidden(Exception ex) {
        return ResponseEntity.status(403).body(StandardResponse.of(403, ex.getMessage(), null));
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<StandardResponse<Void>> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(StandardResponse.of(400, "Datos de entrada inválidos", null));
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<StandardResponse<Map<String, Boolean>>> invalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.of(401, ex.getMessage(), Map.of("login", false)));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<StandardResponse<Void>> duplicateEmail(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(StandardResponse.of(409, ex.getMessage(), null));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ResponseEntity<StandardResponse<Void>> duplicateUsername(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(StandardResponse.of(409, ex.getMessage(), null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<StandardResponse<Void>> userNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.of(404, ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<StandardResponse<Map<String, String>>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(StandardResponse.of(400, "Datos de entrada inválidos", errors));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<StandardResponse<Void>> unexpected(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(StandardResponse.of(500, "Error interno del servidor", null));
    }
}
