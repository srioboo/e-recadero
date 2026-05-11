package org.sirantar.recadero.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard error response DTO for all API error responses.
 * 
 * Provides consistent error formatting across all endpoints.
 * 
 * Fields:
 * - timestamp: When error occurred (ISO 8601)
 * - status: HTTP status code (400, 401, 403, 404, 500, etc.)
 * - error: Error type (VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, etc.)
 * - message: Human-readable error message
 * - path: API path that generated error
 * - errors: List of field-level validation errors (optional)
 * - trace_id: Request trace ID for logging correlation (optional)
 * 
 * Example Response:
 * {
 *   "timestamp": "2026-05-11T10:30:45",
 *   "status": 400,
 *   "error": "VALIDATION_ERROR",
 *   "message": "Input validation failed",
 *   "path": "/api/v1/auth/register",
 *   "errors": [
 *     {"field": "email", "reason": "Invalid email format"},
 *     {"field": "password", "reason": "Password too short"}
 *   ],
 *   "trace_id": "abc-123-def"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private LocalDateTime timestamp;
  private Integer status;
  private String error;
  private String message;
  private String path;

  @JsonProperty("errors")
  private List<FieldError> fieldErrors;

  @JsonProperty("trace_id")
  private String traceId;

  // Additional context for debugging (dev/test only, never in prod)
  @JsonProperty("exception_class")
  private String exceptionClass;

  public ErrorResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ErrorResponse(Integer status, String error, String message, String path) {
    this();
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }

  public ErrorResponse(
      Integer status,
      String error,
      String message,
      String path,
      List<FieldError> fieldErrors
  ) {
    this(status, error, message, path);
    this.fieldErrors = fieldErrors;
  }

  /**
   * Nested DTO for field-level validation errors.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FieldError implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private String reason;

    public FieldError() {
    }

    public FieldError(String field, String reason) {
      this.field = field;
      this.reason = reason;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    @Override
    public String toString() {
      return "FieldError{" +
          "field='" + field + '\'' +
          ", reason='" + reason + '\'' +
          '}';
    }
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<FieldError> getFieldErrors() {
    return fieldErrors;
  }

  public void setFieldErrors(List<FieldError> fieldErrors) {
    this.fieldErrors = fieldErrors;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getExceptionClass() {
    return exceptionClass;
  }

  public void setExceptionClass(String exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "timestamp=" + timestamp +
        ", status=" + status +
        ", error='" + error + '\'' +
        ", message='" + message + '\'' +
        ", path='" + path + '\'' +
        ", fieldErrors=" + fieldErrors +
        ", traceId='" + traceId + '\'' +
        '}';
  }
}
