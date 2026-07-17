package org.sirantar.recadero.cart.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.cart.service.dto.CartUpdatedSummary;
import org.sirantar.recadero.cart.service.dto.RemoveCouponResponse;
import org.sirantar.recadero.cart.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coupon application to a cart, delegating actual validation to
 * {@link CouponValidator} (implemented by the Promotions module).
 */
@Service
@RequiredArgsConstructor
public class CartPromotionService {

  private final CartPromotionRepository cartPromotionRepository;
  private final CartService cartService;
  private final CouponValidator couponValidator;

  @Transactional
  public ApplyCouponResponse applyCoupon(Long userId, String couponCode) {
    Cart cart = cartService.getOrCreateCart(userId);
    BigDecimal subtotal = cartService.calculateTotals(cart.getId()).subtotal();
    List<Long> variantIds = itemVariantIds(userId);

    CouponValidator.CouponValidationResult result = couponValidator.validate(couponCode, subtotal, variantIds, userId);
    if (!result.valid()) {
      if (Boolean.FALSE.equals(result.minimumOrderAmountMet())) {
        throw new ResourceConflictException(
            "COUPON_NOT_APPLICABLE",
            result.reason(),
            Map.of("minimum_order_amount", 0, "current_cart_total", subtotal));
      }
      throw new BusinessLogicException("INVALID_COUPON", result.reason(), Map.of("coupon_code", couponCode));
    }

    cartPromotionRepository.deleteByCartIdAndCouponCode(cart.getId(), couponCode);
    CartPromotion promotion = new CartPromotion();
    promotion.setCartId(cart.getId());
    promotion.setPromotionId(result.promotionId());
    promotion.setCouponCode(couponCode);
    promotion.setDiscountType(result.discountType());
    promotion.setDiscountValue(result.discountValue());
    promotion.setDiscountAmount(result.discountAmount());
    promotion.setAppliedAt(LocalDateTime.now());
    promotion.setAppliedBy(userId.toString());
    cartPromotionRepository.save(promotion);

    var totals = cartService.calculateTotals(cart.getId());
    return new ApplyCouponResponse(
        result.promotionId(),
        couponCode,
        result.discountAmount(),
        result.discountType(),
        result.discountValue(),
        CartUpdatedSummary.forDiscountChange(totals.discountTotal(), totals.grandTotal()));
  }

  @Transactional
  public RemoveCouponResponse removeCoupon(Long userId) {
    Cart cart = cartService.getOrCreateCart(userId);
    cartPromotionRepository.deleteByCartId(cart.getId());
    var totals = cartService.calculateTotals(cart.getId());
    return new RemoveCouponResponse(
        "Coupon removed", CartUpdatedSummary.forDiscountChange(totals.discountTotal(), totals.grandTotal()));
  }

  public ValidateCouponResponse validateCoupon(Long userId, String couponCode) {
    Cart cart = cartService.getOrCreateCart(userId);
    BigDecimal subtotal = cartService.calculateTotals(cart.getId()).subtotal();
    List<Long> variantIds = itemVariantIds(userId);
    CouponValidator.CouponValidationResult result = couponValidator.validate(couponCode, subtotal, variantIds, userId);

    if (!result.valid()) {
      return new ValidateCouponResponse(false, couponCode, null, null, null, false, null);
    }

    return new ValidateCouponResponse(
        true,
        couponCode,
        result.discountAmount(),
        result.discountType(),
        result.discountValue(),
        true,
        new ValidateCouponResponse.Conditions(
            Boolean.TRUE.equals(result.minimumOrderAmountMet()),
            Boolean.TRUE.equals(result.usageLimitNotExceeded()),
            Boolean.TRUE.equals(result.expiryValid())));
  }

  private List<Long> itemVariantIds(Long userId) {
    return cartService.getCart(userId).items().stream()
        .map(org.sirantar.recadero.cart.service.dto.CartItemDetail::productVariantId)
        .toList();
  }
}
