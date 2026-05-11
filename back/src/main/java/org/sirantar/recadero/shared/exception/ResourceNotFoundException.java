package org.sirantar.recadero.shared.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * Maps to HTTP 404 Not Found.
 * 
 * Example:
 * throw new ResourceNotFoundException("User not found with ID: " + userId);
 */
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
