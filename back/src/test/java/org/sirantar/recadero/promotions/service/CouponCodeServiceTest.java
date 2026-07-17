package org.sirantar.recadero.promotions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class CouponCodeServiceTest {

  @Mock private CouponCodeRepository couponCodeRepository;
  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionUsageRepository promotionUsageRepository;
  @Mock private PromotionRulesEngine promotionRulesEngine;
  @Mock private PromotionEventPublisher eventPublisher;

  private CouponCodeService couponCodeService;

  @BeforeEach
  void setUp() {
    couponCodeService = new CouponCodeService(
        couponCodeRepository, promotionRepository, promotionUsageRepository, promotionRulesEngine, eventPublisher);
  }

  @Test
  void validateCouponRejectsUnknownCode() {
    when(couponCodeRepository.findByCode("MISSING")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> couponCodeService.validateCoupon(new ValidateCouponRequest("MISSING", List.of(), BigDecimal.TEN, 1L)))
        .isInstanceOf(BusinessLogicException.class)
        .satisfies(ex -> assertThat(((BusinessLogicException) ex).getErrorCode()).isEqualTo("INVALID_COUPON"));
  }

  @Test
  void validateCouponRejectsExpiredCoupon() {
    Promotion promotion = activePromotion();
    CouponCode coupon = coupon(promotion, LocalDateTime.now().minusDays(1), null);
    when(couponCodeRepository.findByCode("EXPIRED10")).thenReturn(Optional.of(coupon));
    when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

    assertThatThrownBy(() -> couponCodeService.validateCoupon(
        new ValidateCouponRequest("EXPIRED10", List.of(), BigDecimal.TEN, 1L)))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void validateCouponRejectsWhenUsageLimitExceeded() {
    Promotion promotion = activePromotion();
    CouponCode coupon = coupon(promotion, null, 5);
    coupon.setCurrentUsage(5);
    when(couponCodeRepository.findByCode("MAXED")).thenReturn(Optional.of(coupon));
    when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
    when(promotionRulesEngine.evaluatePromotion(any(), any(), any())).thenReturn(true);

    assertThatThrownBy(() -> couponCodeService.validateCoupon(
        new ValidateCouponRequest("MAXED", List.of(), BigDecimal.TEN, 1L)))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void validateCouponAcceptsHealthyCoupon() {
    Promotion promotion = activePromotion();
    CouponCode coupon = coupon(promotion, null, null);
    when(couponCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(coupon));
    when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
    when(promotionRulesEngine.evaluatePromotion(any(), any(), any())).thenReturn(true);
    when(promotionRulesEngine.calculateDiscount(any(), any())).thenReturn(BigDecimal.valueOf(50));

    ValidateCouponResponse response = couponCodeService.validateCoupon(
        new ValidateCouponRequest("WELCOME10", List.of(), BigDecimal.valueOf(100), 1L));

    assertThat(response.isValid()).isTrue();
    assertThat(response.estimatedDiscount()).isEqualByComparingTo("50");
  }

  private Promotion activePromotion() {
    Promotion promotion = new Promotion();
    promotion.setId(1L);
    promotion.setName("Welcome");
    promotion.setType(PromotionType.PERCENTAGE_DISCOUNT);
    promotion.setDiscountValue(BigDecimal.valueOf(50));
    promotion.setStatus(PromotionStatus.ACTIVE);
    promotion.setStartDate(LocalDateTime.now().minusDays(1));
    promotion.setEndDate(LocalDateTime.now().plusDays(10));
    return promotion;
  }

  private CouponCode coupon(Promotion promotion, LocalDateTime expiryDate, Integer usageLimit) {
    CouponCode coupon = new CouponCode();
    coupon.setId(1L);
    coupon.setCode("CODE");
    coupon.setPromotionId(promotion.getId());
    coupon.setIsActive(true);
    coupon.setUsageLimit(usageLimit);
    coupon.setCurrentUsage(0);
    coupon.setExpiryDate(expiryDate);
    coupon.setCreatedAt(LocalDateTime.now());
    return coupon;
  }
}
