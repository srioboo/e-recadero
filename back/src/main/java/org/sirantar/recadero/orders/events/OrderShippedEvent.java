package org.sirantar.recadero.orders.events;

import java.time.LocalDateTime;

/**
 * Published when an order transitions to SHIPPED.
 */
public record OrderShippedEvent(
    Long orderId, String orderNumber, Long userId, String trackingNumber, String carrier, LocalDateTime estimatedDelivery) {}
