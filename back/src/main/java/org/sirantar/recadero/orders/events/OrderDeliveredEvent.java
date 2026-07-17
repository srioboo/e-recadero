package org.sirantar.recadero.orders.events;

import java.time.LocalDateTime;

/**
 * Published when an order transitions to DELIVERED.
 */
public record OrderDeliveredEvent(Long orderId, String orderNumber, Long userId, LocalDateTime deliveredAt) {}
