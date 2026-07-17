package org.sirantar.recadero.cart.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * Placeholder {@link CouponValidator}: the Promotions module doesn't exist
 * yet, so no coupon code can ever be valid. Every apply/validate call fails
 * with the same "not found" reason the real Promotions-backed validator
 * would give for an unknown code — the API contract is honored even though
 * the underlying capability isn't there yet.
 */
@Service
public class NoOpCouponValidator implements CouponValidator {

  @Override
  public CouponValidationResult validate(String couponCode, BigDecimal cartSubtotal) {
    return CouponValidationResult.invalid("Coupon code " + couponCode + " not found or expired");
  }
}
