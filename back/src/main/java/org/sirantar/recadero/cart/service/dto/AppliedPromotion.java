package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * A coupon/promotion currently applied to a cart, as shown in GET /api/v1/cart.
 */
public record AppliedPromotion(
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("discount_amount") BigDecimal discountAmount) {}
