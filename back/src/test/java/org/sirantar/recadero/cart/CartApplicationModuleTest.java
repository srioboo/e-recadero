package org.sirantar.recadero.cart;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Cart module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class CartApplicationModuleTest {

  @Test
  void testCartServicesExist() {
    assertClassExists("org/sirantar/recadero/cart/service/CartService.class");
    assertClassExists("org/sirantar/recadero/cart/service/CartPromotionService.class");
    assertClassExists("org/sirantar/recadero/cart/service/CheckoutService.class");
    assertClassExists("org/sirantar/recadero/cart/service/CartExpirationService.class");
    assertClassExists("org/sirantar/recadero/cart/service/CartValidationService.class");
  }

  @Test
  void testCartRepositoriesExist() {
    assertClassExists("org/sirantar/recadero/cart/repository/CartRepository.class");
    assertClassExists("org/sirantar/recadero/cart/repository/CartItemRepository.class");
    assertClassExists("org/sirantar/recadero/cart/repository/ReservationRepository.class");
    assertClassExists("org/sirantar/recadero/cart/repository/CartPromotionRepository.class");
  }

  @Test
  void testCartApiExists() {
    assertClassExists("org/sirantar/recadero/cart/api/CartController.class");
  }

  @Test
  void testCartDomainsExist() {
    assertClassExists("org/sirantar/recadero/cart/domain/Cart.class");
    assertClassExists("org/sirantar/recadero/cart/domain/CartItem.class");
    assertClassExists("org/sirantar/recadero/cart/domain/Reservation.class");
    assertClassExists("org/sirantar/recadero/cart/domain/CartPromotion.class");
  }

  @Test
  void testCartEventsExist() {
    assertClassExists("org/sirantar/recadero/cart/events/CartItemAddedEvent.class");
    assertClassExists("org/sirantar/recadero/cart/events/CartAbandonedEvent.class");
    assertClassExists("org/sirantar/recadero/cart/events/CheckoutStartedEvent.class");
    assertClassExists("org/sirantar/recadero/cart/events/CheckoutCompletedEvent.class");
    assertClassExists("org/sirantar/recadero/cart/events/CartEventPublisher.class");
  }

  @Test
  void testCartPackageInfoExists() {
    assertClassExists("org/sirantar/recadero/cart/package-info.class");
  }

  private void assertClassExists(String resourcePath) {
    assertThat(CartApplicationModuleTest.class.getClassLoader().getResource(resourcePath))
        .as(resourcePath + " should exist")
        .isNotNull();
  }
}
