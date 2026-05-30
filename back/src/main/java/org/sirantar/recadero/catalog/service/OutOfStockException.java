package org.sirantar.recadero.catalog.service;

/**
 * Raised when inventory is insufficient for a reservation.
 */
public class OutOfStockException extends RuntimeException {

  public OutOfStockException(String message) {
    super(message);
  }
}
