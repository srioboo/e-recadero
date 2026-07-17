package org.sirantar.recadero.orders.service.dto;

import java.math.BigDecimal;

/**
 * Request payload for POST /api/v1/orders/{order_id}/refund.
 */
public record RefundRequest(BigDecimal amount, String reason) {}
