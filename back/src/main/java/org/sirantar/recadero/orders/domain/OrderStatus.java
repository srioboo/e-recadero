package org.sirantar.recadero.orders.domain;

/**
 * Lifecycle status of an order.
 */
public enum OrderStatus {
  PENDING,
  CONFIRMED,
  SHIPPED,
  DELIVERED,
  CANCELLED,
  REFUNDED
}
