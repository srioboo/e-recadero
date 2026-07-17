package org.sirantar.recadero.cart.domain;

/**
 * Lifecycle status of a shopping cart.
 */
public enum CartStatus {
  ACTIVE,
  LOCKED_FOR_CHECKOUT,
  CHECKED_OUT,
  ABANDONED
}
