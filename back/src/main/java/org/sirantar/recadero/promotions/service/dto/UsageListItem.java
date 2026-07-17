package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/promotions/{id}/usage.
 */
public record UsageListItem(
    @JsonProperty("usage_id") Long usageId,
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("discount_amount") BigDecimal discountAmount,
    @JsonProperty("used_at") LocalDateTime usedAt) {}
