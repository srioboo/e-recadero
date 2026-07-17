package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Request payload for POST /api/v1/coupons/{code}/apply (called by Orders module).
 */
public record ApplyCouponRequest(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("discount_amount") BigDecimal discountAmount) {}
