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

  public ResourceConflictException(String message) {
    super(message);
  }

  public ResourceConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
