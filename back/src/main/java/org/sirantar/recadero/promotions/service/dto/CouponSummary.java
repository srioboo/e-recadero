package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Row shape for a coupon code, embedded in promotion detail and coupon list responses.
 */
public record CouponSummary(
    @JsonProperty("coupon_id") Long couponId,
    String code,
    @JsonProperty("usage_limit") Integer usageLimit,
    @JsonProperty("current_usage") int currentUsage,
    @JsonProperty("is_active") boolean isActive,
    @JsonProperty("expiry_date") LocalDateTime expiryDate,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
