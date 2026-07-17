package org.sirantar.recadero.promotions.events;

import java.math.BigDecimal;

/**
 * Published when a coupon is applied to an order.
 */
public record CouponUsedEvent(String couponCode, Long promotionId, Long orderId, Long userId, BigDecimal discountAmount) {}
