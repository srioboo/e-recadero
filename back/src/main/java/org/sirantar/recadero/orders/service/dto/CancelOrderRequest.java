package org.sirantar.recadero.orders.service.dto;

/**
 * Request payload for PATCH /api/v1/orders/{order_id}/cancel.
 */
public record CancelOrderRequest(String reason) {}
