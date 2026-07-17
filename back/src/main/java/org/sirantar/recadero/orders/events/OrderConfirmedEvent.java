package org.sirantar.recadero.orders.events;

import java.math.BigDecimal;

/**
 * Published when an order is created and confirmed (payment already succeeded upstream).
 */
public record OrderConfirmedEvent(Long orderId, String orderNumber, Long userId, BigDecimal grandTotal) {}
