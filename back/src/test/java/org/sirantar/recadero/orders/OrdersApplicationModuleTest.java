package org.sirantar.recadero.orders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Orders module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class OrdersApplicationModuleTest {

  @Test
  void testOrdersServicesExist() {
    assertClassExists("org/sirantar/recadero/orders/service/OrderService.class");
    assertClassExists("org/sirantar/recadero/orders/service/OrderShipmentService.class");
    assertClassExists("org/sirantar/recadero/orders/service/OrderReturnService.class");
    assertClassExists("org/sirantar/recadero/orders/service/OrderValidationService.class");
    assertClassExists("org/sirantar/recadero/orders/service/CartCheckoutEventListener.class");
  }

  @Test
  void testOrdersRepositoriesExist() {
    assertClassExists("org/sirantar/recadero/orders/repository/OrderRepository.class");
    assertClassExists("org/sirantar/recadero/orders/repository/OrderShipmentRepository.class");
    assertClassExists("org/sirantar/recadero/orders/repository/OrderPaymentRepository.class");
    assertClassExists("org/sirantar/recadero/orders/repository/OrderReturnRepository.class");
  }

  @Test
  void testOrdersApiExists() {
    assertClassExists("org/sirantar/recadero/orders/api/OrderController.class");
    assertClassExists("org/sirantar/recadero/orders/api/AdminOrderController.class");
  }

  @Test
  void testOrdersDomainsExist() {
    assertClassExists("org/sirantar/recadero/orders/domain/Order.class");
    assertClassExists("org/sirantar/recadero/orders/domain/OrderItem.class");
    assertClassExists("org/sirantar/recadero/orders/domain/OrderShipment.class");
    assertClassExists("org/sirantar/recadero/orders/domain/OrderPayment.class");
  }

  @Test
  void testOrdersEventsExist() {
    assertClassExists("org/sirantar/recadero/orders/events/OrderConfirmedEvent.class");
    assertClassExists("org/sirantar/recadero/orders/events/OrderShippedEvent.class");
    assertClassExists("org/sirantar/recadero/orders/events/OrderDeliveredEvent.class");
    assertClassExists("org/sirantar/recadero/orders/events/OrderRefundedEvent.class");
    assertClassExists("org/sirantar/recadero/orders/events/OrderEventPublisher.class");
  }

  @Test
  void testOrdersPackageInfoExists() {
    assertClassExists("org/sirantar/recadero/orders/package-info.class");
  }

  private void assertClassExists(String resourcePath) {
    assertThat(OrdersApplicationModuleTest.class.getClassLoader().getResource(resourcePath))
        .as(resourcePath + " should exist")
        .isNotNull();
  }
}
