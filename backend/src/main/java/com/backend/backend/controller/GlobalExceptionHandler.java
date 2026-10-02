package com.backend.backend.controller;

import com.backend.backend.exception.AirlineNotFoundException;
import com.backend.backend.exception.AirportNotFoundException;
import com.backend.backend.exception.GraphServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions thrown by controllers and services into consistent JSON error responses.
 *
 * <p>Response body shape:
 *
 * <pre>
 * {
 *   "timestamp": "2026-10-02T12:34:56Z",
 *   "status":    404,
 *   "error":     "Not Found",
 *   "message":   "Airport not found: XXX",
 *   "path":      "/api/airports/XXX"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AirportNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      AirportNotFoundException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(
      IllegalArgumentException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    String message =
        ex.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getMessage())
            .orElse("Invalid request");
    return buildResponse(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(
      Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
  }

  private static ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String message, HttpServletRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(GraphServiceUnavailableException.class)
  public ResponseEntity<Map<String, Object>> handleGraphUnavailable(
      GraphServiceUnavailableException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
  }

  @ExceptionHandler(AirlineNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleAirlineNotFound(
      AirlineNotFoundException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }
}
