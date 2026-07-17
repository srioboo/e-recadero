package org.sirantar.recadero.promotions.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.service.CouponValidator;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;

/**
 * Real implementation of Cart's {@link CouponValidator} port, backed by
 * this module's coupon/promotion rules engine. See
 * cart-contract.md's "Cross-Module Dependencies: Promotions Module".
 */
@Service
@RequiredArgsConstructor
public class PromotionsCouponValidator implements CouponValidator {

  private final CouponCodeService couponCodeService;

  @Override
  public CouponValidationResult validate(
      String couponCode, BigDecimal cartSubtotal, List<Long> productVariantIds, Long userId) {
    List<ValidateCouponRequest.CartItemPayload> items = productVariantIds.stream()
        .map(id -> new ValidateCouponRequest.CartItemPayload(null, id, null, 1, null))
        .toList();

    ValidateCouponResponse response;
    try {
      response = couponCodeService.validateCoupon(new ValidateCouponRequest(couponCode, items, cartSubtotal, userId));
    } catch (BusinessLogicException | ResourceConflictException e) {
      return CouponValidationResult.invalid(e.getMessage());
    }

    Boolean minimumOrderMet = response.conditionsMet() != null ? response.conditionsMet().get("minimum_order_met") : null;
    Boolean usageOk = response.conditionsMet() != null ? response.conditionsMet().get("usage_limit_not_exceeded") : null;
    Boolean expiryOk = response.conditionsMet() != null ? response.conditionsMet().get("expiry_valid") : null;

    return new CouponValidationResult(
        true,
        null,
        response.promotionId(),
        response.discountType(),
        response.discountValue(),
        response.estimatedDiscount(),
        minimumOrderMet,
        usageOk,
        expiryOk);
  }
}
