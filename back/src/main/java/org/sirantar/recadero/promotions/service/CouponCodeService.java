package org.sirantar.recadero.promotions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionUsage;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.sirantar.recadero.promotions.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsRequest;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsResponse;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coupon code generation, validation (no side effects), and application
 * (usage tracking) against their parent promotion.
 */
@Service
@RequiredArgsConstructor
public class CouponCodeService {

  private final CouponCodeRepository couponCodeRepository;
  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final PromotionRulesEngine promotionRulesEngine;
  private final PromotionEventPublisher eventPublisher;

  @Transactional
  public GenerateCouponsResponse generateCoupons(Long promotionId, GenerateCouponsRequest request) {
    Promotion promotion = promotionRepository.findById(promotionId)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionId));

    String prefix = request.codePrefix() != null && !request.codePrefix().isBlank() ? request.codePrefix() : "PROMO";
    List<String> codes = new java.util.ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (int i = 1; i <= request.count(); i++) {
      String code = prefix + "-" + String.format("%03d", i) + "-" + randomSuffix();
      CouponCode coupon = new CouponCode();
      coupon.setCode(code);
      coupon.setPromotionId(promotion.getId());
      coupon.setIsActive(true);
      coupon.setUsageLimit(request.usageLimit());
      coupon.setExpiryDate(request.expiryDate());
      coupon.setCreatedAt(now);
      couponCodeRepository.save(coupon);
      codes.add(code);
    }

    return new GenerateCouponsResponse(
        codes.size(), codes, "http://api.example.com/admin/coupons/batch-" + promotionId + "/download");
  }

  public ValidateCouponResponse validateCoupon(ValidateCouponRequest request) {
    Optional<CouponCode> couponOpt = couponCodeRepository.findByCode(request.couponCode());
    if (couponOpt.isEmpty()) {
      throw new BusinessLogicException(
          "INVALID_COUPON", "Coupon code " + request.couponCode() + " not found", Map.of("conditions_met", Map.of()));
    }
    CouponCode coupon = couponOpt.get();
    Promotion promotion = promotionRepository.findById(coupon.getPromotionId())
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found for coupon: " + request.couponCode()));

    List<Long> variantIds = request.cartItems() == null
        ? List.of()
        : request.cartItems().stream().map(ValidateCouponRequest.CartItemPayload::productVariantId).toList();

    boolean campaignActive = promotion.getStatus() == PromotionStatus.ACTIVE
        && !promotion.getStartDate().isAfter(LocalDateTime.now())
        && (promotion.getEndDate() == null || !promotion.getEndDate().isBefore(LocalDateTime.now()));
    boolean expiryValid = coupon.getExpiryDate() == null || coupon.getExpiryDate().isAfter(LocalDateTime.now());
    boolean usageLimitOk = coupon.getUsageLimit() == null || coupon.getCurrentUsage() < coupon.getUsageLimit();
    boolean minimumOrderMet = promotion.getMinimumOrderAmount() == null
        || (request.subtotal() != null && request.subtotal().compareTo(promotion.getMinimumOrderAmount()) >= 0);
    boolean productEligible = promotionRulesEngine.evaluatePromotion(promotion, variantIds, request.userId());

    Map<String, Boolean> conditions = new java.util.LinkedHashMap<>();
    conditions.put("minimum_order_met", minimumOrderMet);
    conditions.put("product_eligible", productEligible);
    conditions.put("usage_limit_not_exceeded", usageLimitOk);
    conditions.put("expiry_valid", expiryValid);
    conditions.put("campaign_active", campaignActive);

    boolean isValid = Boolean.TRUE.equals(coupon.getIsActive())
        && campaignActive && expiryValid && usageLimitOk && minimumOrderMet && productEligible;

    if (!isValid) {
      if (!minimumOrderMet) {
        throw new ResourceConflictException(
            "COUPON_NOT_APPLICABLE",
            "Coupon requires minimum order of $" + promotion.getMinimumOrderAmount()
                + "; current subtotal is $" + request.subtotal(),
            Map.of("conditions_met", conditions));
      }
      throw new BusinessLogicException("INVALID_COUPON", "Coupon code " + request.couponCode() + " is not valid",
          Map.of("conditions_met", conditions));
    }

    BigDecimal estimatedDiscount = promotionRulesEngine.calculateDiscount(promotion, request.subtotal());

    return new ValidateCouponResponse(
        true,
        coupon.getCode(),
        promotion.getId(),
        promotion.getType().name(),
        promotion.getDiscountValue(),
        promotion.getMaxDiscountAmount(),
        estimatedDiscount,
        conditions);
  }

  @Transactional
  public ApplyCouponResponse applyCoupon(String code, Long orderId, Long userId, BigDecimal discountAmount) {
    CouponCode coupon = couponCodeRepository.findByCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
    Promotion promotion = promotionRepository.findById(coupon.getPromotionId())
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found for coupon: " + code));

    if (coupon.getUsageLimit() != null && coupon.getCurrentUsage() >= coupon.getUsageLimit()) {
      throw new ResourceConflictException(
          "USAGE_LIMIT_EXCEEDED",
          "Coupon usage limit reached",
          Map.of("coupon_code", code, "usage_limit", coupon.getUsageLimit(), "current_usage", coupon.getCurrentUsage()));
    }

    coupon.setCurrentUsage(coupon.getCurrentUsage() + 1);
    couponCodeRepository.save(coupon);

    promotion.setCurrentUsageCount(promotion.getCurrentUsageCount() + 1);
    promotionRepository.save(promotion);

    LocalDateTime now = LocalDateTime.now();
    PromotionUsage usage = new PromotionUsage();
    usage.setPromotionId(promotion.getId());
    usage.setCouponCodeId(coupon.getId());
    usage.setOrderId(orderId);
    usage.setUserId(userId);
    usage.setDiscountAmount(discountAmount);
    usage.setUsedAt(now);
    PromotionUsage saved = promotionUsageRepository.save(usage);

    eventPublisher.publishCouponUsed(code, promotion.getId(), orderId, userId, discountAmount);

    return new ApplyCouponResponse(saved.getId(), promotion.getId(), code, discountAmount, now);
  }

  private String randomSuffix() {
    return java.util.UUID.randomUUID().toString().substring(0, 3).toUpperCase();
  }
}
