package org.sirantar.recadero.shared.exception;

/**
 * Exception thrown for business logic validation failures.
 * 
 * Maps to HTTP 400 Bad Request.
 * 
 * Supports error codes for programmatic handling:
 * - INSUFFICIENT_INVENTORY: Not enough items in stock
 * - INVALID_STATUS_TRANSITION: Order status change not allowed
 * - EXPIRED_TOKEN: Email verification token expired
 * - DUPLICATE_COUPON: Coupon code already exists
 * - MAX_ADDRESSES_EXCEEDED: User reached address limit
 * 
 * Example:
 * throw new BusinessLogicException("INSUFFICIENT_INVENTORY", "Only 5 items available");
 */
public class BusinessLogicException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String errorCode;

  public BusinessLogicException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BusinessLogicException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
