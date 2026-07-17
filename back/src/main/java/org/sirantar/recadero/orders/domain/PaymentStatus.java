package org.sirantar.recadero.orders.domain;

/**
 * Status of an order's payment.
 */
public enum PaymentStatus {
  PENDING,
  AUTHORIZED,
  CAPTURED,
  FAILED,
  REFUNDED,
  PARTIALLY_REFUNDED
}
