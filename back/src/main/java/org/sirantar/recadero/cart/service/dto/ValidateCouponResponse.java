package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for GET /api/v1/cart/validate-coupon.
 */
public record ValidateCouponResponse(
    @JsonProperty("is_valid") boolean isValid,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("discount_amount") BigDecimal discountAmount,
    @JsonProperty("discount_type") String discountType,
    @JsonProperty("discount_value") BigDecimal discountValue,
    @JsonProperty("conditions_met") boolean conditionsMet,
    Conditions conditions) {

  public record Conditions(
      @JsonProperty("minimum_order_amount_met") boolean minimumOrderAmountMet,
      @JsonProperty("usage_limit_not_exceeded") boolean usageLimitNotExceeded,
      @JsonProperty("expiry_valid") boolean expiryValid) {}
}
