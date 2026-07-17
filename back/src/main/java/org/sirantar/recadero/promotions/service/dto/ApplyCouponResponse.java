package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for POST /api/v1/coupons/{code}/apply.
 */
public record ApplyCouponResponse(
    @JsonProperty("promotion_usage_id") Long promotionUsageId,
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("discount_amount") BigDecimal discountAmount,
    @JsonProperty("applied_at") LocalDateTime appliedAt) {}
