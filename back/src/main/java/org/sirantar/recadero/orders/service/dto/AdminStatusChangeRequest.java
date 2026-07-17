package org.sirantar.recadero.orders.service.dto;

/**
 * Request payload for PATCH /api/v1/admin/orders/{order_id}/status.
 */
public record AdminStatusChangeRequest(String status, String notes) {}
