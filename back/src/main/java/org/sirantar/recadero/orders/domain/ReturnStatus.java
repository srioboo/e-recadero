package org.sirantar.recadero.orders.domain;

/**
 * Status of a return/RMA request.
 */
public enum ReturnStatus {
  PENDING_APPROVAL,
  APPROVED,
  REJECTED,
  COMPLETED
}
