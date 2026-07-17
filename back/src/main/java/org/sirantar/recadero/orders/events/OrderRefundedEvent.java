package org.sirantar.recadero.orders.events;

import java.math.BigDecimal;

/**
 * Published when an order is refunded (fully or partially).
 */
public record OrderRefundedEvent(Long orderId, BigDecimal refundAmount, String reason) {}
