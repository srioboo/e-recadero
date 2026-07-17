package org.sirantar.recadero.orders.events;

import java.math.BigDecimal;

/**
 * Published when an order is created and confirmed (payment already succeeded upstream).
 * {@code couponCode}/{@code discountAmount} let the Promotions module record
 * real usage against this order without Orders needing to depend on it.
 */
public record OrderConfirmedEvent(
    Long orderId, String orderNumber, Long userId, BigDecimal grandTotal, String couponCode, BigDecimal discountAmount) {}
