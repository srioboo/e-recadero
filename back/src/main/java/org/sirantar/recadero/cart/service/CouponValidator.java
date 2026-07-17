package org.sirantar.recadero.cart.service;

import java.math.BigDecimal;

/**
 * Port for validating a coupon code against the Promotions module. Phase 7
 * (Promotions) is not yet implemented, so {@link NoOpCouponValidator} is the
 * only implementation for now — swap it for a real client once Promotions
 * exposes its coupon-validation endpoint (see cart-contract.md's
 * "Cross-Module Dependencies": Promotions Module).
 */
public interface CouponValidator {

  CouponValidationResult validate(String couponCode, BigDecimal cartSubtotal);

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
