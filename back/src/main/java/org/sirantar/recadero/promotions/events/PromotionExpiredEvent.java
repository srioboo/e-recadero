package org.sirantar.recadero.promotions.events;

import java.math.BigDecimal;

/**
 * Published when a promotion auto-expires (end_date reached).
 */
public record PromotionExpiredEvent(Long promotionId, String promotionName, int totalUsage, BigDecimal totalDiscount) {}
