package org.sirantar.recadero.promotions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionRule;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.sirantar.recadero.promotions.service.CouponCodeService;
import org.sirantar.recadero.promotions.service.PromotionRulesEngine;
import org.sirantar.recadero.promotions.service.PromotionService;
import org.sirantar.recadero.promotions.service.PromotionValidationService;
import org.sirantar.recadero.promotions.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionRequest;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionResponse;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsRequest;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsResponse;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponResponse;

/**
 * End-to-end workflow test for the Promotions module: create promotion →
 * generate coupons → validate coupon → apply to order, exercising the
 * service layer with mocked persistence (mirrors the other modules'
 * *IntegrationTest style).
 */
@DisplayName("Promotions Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class PromotionIntegrationTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionRuleRepository promotionRuleRepository;
  @Mock private CouponCodeRepository couponCodeRepository;
  @Mock private PromotionUsageRepository promotionUsageRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private PromotionEventPublisher eventPublisher;

  private PromotionService promotionService;
  private CouponCodeService couponCodeService;

  private Promotion savedPromotion;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    PromotionRulesEngine rulesEngine = new PromotionRulesEngine(promotionRuleRepository, productVariantRepository, orderRepository, objectMapper);
    promotionService = new PromotionService(
        promotionRepository, promotionRuleRepository, couponCodeRepository,
        new PromotionValidationService(), eventPublisher, objectMapper);
    couponCodeService = new CouponCodeService(couponCodeRepository, promotionRepository, promotionUsageRepository, rulesEngine, eventPublisher);

    when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> {
      Promotion p = inv.getArgument(0);
      if (p.getId() == null) {
        p.setId(1L);
        savedPromotion = p;
      }
      return p;
    });
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of());
  }

  @Test
  @DisplayName("Should create promotion, generate coupons, validate a coupon, and apply it to an order")
  void createGenerateValidateApplyFlow() {
    CreatePromotionRequest request = new CreatePromotionRequest(
        "Welcome 10%", "PERCENTAGE_DISCOUNT", BigDecimal.TEN, null, null,
        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30), 100, 10, List.of());

    CreatePromotionResponse created = promotionService.createPromotion(request, "admin-1");
    assertThat(created.status()).isEqualTo("ACTIVE");
    when(promotionRepository.findById(1L)).thenAnswer(inv -> Optional.of(savedPromotion));

    GenerateCouponsResponse coupons = couponCodeService.generateCoupons(
        1L, new GenerateCouponsRequest(3, "WELCOME", 1, null));
    assertThat(coupons.generatedCount()).isEqualTo(3);
    assertThat(coupons.couponCodes()).hasSize(3);

    String issuedCode = coupons.couponCodes().get(0);
    CouponCode couponEntity = new CouponCode();
    couponEntity.setId(1L);
    couponEntity.setCode(issuedCode);
    couponEntity.setPromotionId(1L);
    couponEntity.setIsActive(true);
    couponEntity.setUsageLimit(1);
    couponEntity.setCurrentUsage(0);
    couponEntity.setCreatedAt(LocalDateTime.now());
    when(couponCodeRepository.findByCode(issuedCode)).thenReturn(Optional.of(couponEntity));

    ValidateCouponResponse validation = couponCodeService.validateCoupon(
        new ValidateCouponRequest(issuedCode, List.of(), BigDecimal.valueOf(100), 42L));
    assertThat(validation.isValid()).isTrue();
    assertThat(validation.estimatedDiscount()).isEqualByComparingTo("10.00");

    when(couponCodeRepository.save(any(CouponCode.class))).thenAnswer(inv -> inv.getArgument(0));
    when(promotionUsageRepository.save(any())).thenAnswer(inv -> {
      var usage = inv.getArgument(0, org.sirantar.recadero.promotions.domain.PromotionUsage.class);
      usage.setId(1L);
      return usage;
    });

    ApplyCouponResponse applied = couponCodeService.applyCoupon(issuedCode, 500L, 42L, BigDecimal.valueOf(10));
    assertThat(applied.promotionId()).isEqualTo(1L);
    assertThat(couponEntity.getCurrentUsage()).isEqualTo(1);
    assertThat(savedPromotion.getCurrentUsageCount()).isEqualTo(1);
  }
}
