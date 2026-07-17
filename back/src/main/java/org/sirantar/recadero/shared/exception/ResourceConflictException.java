package org.sirantar.recadero.shared.exception;

/**
 * Exception thrown when resource operation conflicts with existing state.
 * 
 * Maps to HTTP 409 Conflict.
 * 
 * Use cases:
 * - Duplicate resource: User with email already exists
 * - Optimistic lock failure: Entity version mismatch
 * - State conflict: Cannot cancel order already shipped
 * - Unique constraint violation: Username already taken
 * 
 * Example:
 * throw new ResourceConflictException("Email already registered: " + email);
 */
public class ResourceConflictException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String errorCode;
  private final java.util.Map<String, Object> details;

  public ResourceConflictException(String message) {
    super(message);
    this.errorCode = null;
    this.details = null;
  }

  public ResourceConflictException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = null;
    this.details = null;
  }

  public ResourceConflictException(String errorCode, String message, java.util.Map<String, Object> details) {
    super(message);
    this.errorCode = errorCode;
    this.details = details;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public java.util.Map<String, Object> getDetails() {
    return details;
  }
}
