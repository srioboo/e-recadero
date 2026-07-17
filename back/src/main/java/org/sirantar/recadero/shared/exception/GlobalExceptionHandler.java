package org.sirantar.recadero.shared.exception;

import org.sirantar.recadero.shared.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all APIs.
 * 
 * Centralizes error handling across the application:
 * - Validation errors (400)
 * - Authentication errors (401)
 * - Authorization errors (403)
 * - Not found errors (404)
 * - Internal server errors (500)
 * - Custom business logic errors
 * 
 * Features:
 * - Consistent error response format (ErrorResponse DTO)
 * - Field-level validation error details
 * - Request trace IDs for logging correlation
 * - Environment-aware exception details (dev shows stack trace, prod hides)
 * - Structured logging for all errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  /**
   * Handle validation errors from @Valid annotation.
   * 
   * Maps field-level validation errors to ErrorResponse with details.
   * Status: 400 Bad Request
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new ErrorResponse.FieldError(
            error.getField(),
            error.getDefaultMessage()
        ))
        .collect(Collectors.toList());

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "VALIDATION_ERROR",
        "Input validation failed",
        request.getRequestURI(),
        fieldErrors
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Validation error [trace_id={}]: {}", traceId, fieldErrors);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handle type mismatch errors (e.g., invalid UUID format).
   * 
   * Status: 400 Bad Request
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    String message = String.format(
        "Invalid value for parameter '%s': expected %s but got '%s'",
        ex.getName(),
        ex.getRequiredType().getSimpleName(),
        ex.getValue()
    );

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "TYPE_MISMATCH_ERROR",
        message,
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Type mismatch error [trace_id={}]: {}", traceId, message);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handle authentication errors (invalid credentials, expired JWT).
   * 
   * Status: 401 Unauthorized
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        "AUTHENTICATION_ERROR",
        "Invalid credentials or expired token",
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Authentication error [trace_id={}]: {}", traceId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handle authorization errors (access denied).
   * 
   * Status: 403 Forbidden
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        "ACCESS_DENIED",
        "You do not have permission to access this resource",
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Access denied error [trace_id={}]", traceId);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /**
   * Handle 404 Not Found errors.
   * 
   * Status: 404 Not Found
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
      NoHandlerFoundException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        "NOT_FOUND",
        String.format("Endpoint not found: %s %s", ex.getHttpMethod(), ex.getRequestURL()),
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Not found error [trace_id={}]: {} {}", traceId, ex.getHttpMethod(), ex.getRequestURL());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handle custom business logic exceptions (ResourceNotFoundException, etc.).
   * 
   * Status: 404 Not Found
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        "RESOURCE_NOT_FOUND",
        ex.getMessage(),
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Resource not found [trace_id={}]: {}", traceId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handle business logic validation errors.
   * 
   * Status: 400 Bad Request
   */
  @ExceptionHandler(BusinessLogicException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleBusinessLogicException(
      BusinessLogicException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI()
    );
    errorResponse.setErrorCode(ex.getErrorCode());
    errorResponse.setDetails(ex.getDetails());
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Business logic error [trace_id={}] [code={}]: {}",
        traceId, ex.getErrorCode(), ex.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handle conflict errors (duplicate resource, state conflict).
   * 
   * Status: 409 Conflict
   */
  @ExceptionHandler(ResourceConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        ex.getErrorCode() != null ? ex.getErrorCode() : "RESOURCE_CONFLICT",
        ex.getMessage(),
        request.getRequestURI()
    );
    errorResponse.setErrorCode(ex.getErrorCode());
    errorResponse.setDetails(ex.getDetails());
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.warn("Resource conflict [trace_id={}]: {}", traceId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /**
   * Handle unspecified exceptions (catch-all).
   * 
   * Status: 500 Internal Server Error
   * 
   * Never exposes internal exception details in production.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex,
      HttpServletRequest request
  ) {
    String traceId = generateTraceId();
    
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "INTERNAL_SERVER_ERROR",
        "An unexpected error occurred. Please contact support with trace ID: " + traceId,
        request.getRequestURI()
    );
    errorResponse.setTraceId(traceId);

    if (isDevEnvironment()) {
      errorResponse.setExceptionClass(ex.getClass().getSimpleName());
    }

    log.error("Unhandled exception [trace_id={}]", traceId, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Generate unique trace ID for request correlation.
   * 
   * @return UUID trace ID
   */
  private String generateTraceId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Check if running in development environment.
   * 
   * @return true if dev profile active
   */
  private boolean isDevEnvironment() {
    return "dev".equalsIgnoreCase(activeProfile) || "test".equalsIgnoreCase(activeProfile);
  }
}
