package org.sirantar.recadero.cart.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port for validating a coupon code against the Promotions module.
 * Implemented by {@code promotions.service.PromotionsCouponValidator} (Cart
 * owns this interface and depends on nothing from Promotions directly —
 * Promotions provides the adapter, per the dependency-inversion pattern).
 */
public interface CouponValidator {

  CouponValidationResult validate(
      String couponCode, BigDecimal cartSubtotal, List<Long> productVariantIds, Long userId);

  record CouponValidationResult(
      boolean valid,
      String reason,
      Long promotionId,
      String discountType,
      BigDecimal discountValue,
      BigDecimal discountAmount,
      Boolean minimumOrderAmountMet,
      Boolean usageLimitNotExceeded,
      Boolean expiryValid) {

    public static CouponValidationResult invalid(String reason) {
      return new CouponValidationResult(false, reason, null, null, null, null, null, null, null);
    }
  }
}
