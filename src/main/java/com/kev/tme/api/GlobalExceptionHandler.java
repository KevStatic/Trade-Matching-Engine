package com.kev.tme.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates bad input into a 400 with a clear, machine-readable message body
 * instead of leaking a stack trace or a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : error.getDefaultMessage())
                .collect(Collectors.toList());
        return badRequest("Validation failed", messages);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return badRequest("Malformed request body", List.of(rootMessage(ex)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest("Invalid request", List.of(String.valueOf(ex.getMessage())));
    }

    private ResponseEntity<Map<String, Object>> badRequest(String error, List<String> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", error);
        body.put("messages", messages);
        return ResponseEntity.badRequest().body(body);
    }

    private String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
