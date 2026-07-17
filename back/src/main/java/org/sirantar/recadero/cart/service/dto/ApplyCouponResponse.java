package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for POST /api/v1/cart/apply-coupon.
 */
public record ApplyCouponResponse(
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("discount_amount") BigDecimal discountAmount,
    @JsonProperty("discount_type") String discountType,
    @JsonProperty("discount_value") BigDecimal discountValue,
    @JsonProperty("cart_updated") CartUpdatedSummary cartUpdated) {}
