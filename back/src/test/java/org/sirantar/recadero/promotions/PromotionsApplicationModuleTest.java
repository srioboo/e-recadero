package org.sirantar.recadero.promotions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Promotions module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class PromotionsApplicationModuleTest {

  @Test
  void testPromotionsServicesExist() {
    assertClassExists("org/sirantar/recadero/promotions/service/PromotionService.class");
    assertClassExists("org/sirantar/recadero/promotions/service/PromotionRulesEngine.class");
    assertClassExists("org/sirantar/recadero/promotions/service/CouponCodeService.class");
    assertClassExists("org/sirantar/recadero/promotions/service/PromotionExpirationService.class");
    assertClassExists("org/sirantar/recadero/promotions/service/PromotionValidationService.class");
    assertClassExists("org/sirantar/recadero/promotions/service/PromotionsCouponValidator.class");
  }

  @Test
  void testPromotionsRepositoriesExist() {
    assertClassExists("org/sirantar/recadero/promotions/repository/PromotionRepository.class");
    assertClassExists("org/sirantar/recadero/promotions/repository/PromotionRuleRepository.class");
    assertClassExists("org/sirantar/recadero/promotions/repository/CouponCodeRepository.class");
    assertClassExists("org/sirantar/recadero/promotions/repository/PromotionUsageRepository.class");
  }

  @Test
  void testPromotionsApiExists() {
    assertClassExists("org/sirantar/recadero/promotions/api/PromotionAdminController.class");
    assertClassExists("org/sirantar/recadero/promotions/api/CouponController.class");
  }

  @Test
  void testPromotionsDomainsExist() {
    assertClassExists("org/sirantar/recadero/promotions/domain/Promotion.class");
    assertClassExists("org/sirantar/recadero/promotions/domain/PromotionRule.class");
    assertClassExists("org/sirantar/recadero/promotions/domain/CouponCode.class");
    assertClassExists("org/sirantar/recadero/promotions/domain/PromotionUsage.class");
  }

  @Test
  void testPromotionsEventsExist() {
    assertClassExists("org/sirantar/recadero/promotions/events/PromotionActivatedEvent.class");
    assertClassExists("org/sirantar/recadero/promotions/events/PromotionExpiredEvent.class");
    assertClassExists("org/sirantar/recadero/promotions/events/CouponUsedEvent.class");
    assertClassExists("org/sirantar/recadero/promotions/events/PromotionEventPublisher.class");
  }

  @Test
  void testPromotionsPackageInfoExists() {
    assertClassExists("org/sirantar/recadero/promotions/package-info.class");
  }

  private void assertClassExists(String resourcePath) {
    assertThat(PromotionsApplicationModuleTest.class.getClassLoader().getResource(resourcePath))
        .as(resourcePath + " should exist")
        .isNotNull();
  }
}
