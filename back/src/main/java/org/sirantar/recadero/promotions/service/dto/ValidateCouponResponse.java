package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Response payload for POST /api/v1/coupons/validate.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidateCouponResponse(
    @JsonProperty("is_valid") boolean isValid,
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("discount_type") String discountType,
    @JsonProperty("discount_value") BigDecimal discountValue,
    @JsonProperty("max_discount_amount") BigDecimal maxDiscountAmount,
    @JsonProperty("estimated_discount") BigDecimal estimatedDiscount,
    @JsonProperty("conditions_met") Map<String, Boolean> conditionsMet) {}
