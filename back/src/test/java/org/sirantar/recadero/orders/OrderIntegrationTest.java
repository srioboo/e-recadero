package org.sirantar.recadero.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.sirantar.recadero.orders.domain.OrderShipment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.ShipmentStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderPaymentRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderReturnRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentEventRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.repository.OrderTransactionRepository;
import org.sirantar.recadero.orders.service.OrderReturnService;
import org.sirantar.recadero.orders.service.OrderService;
import org.sirantar.recadero.orders.service.OrderShipmentService;
import org.sirantar.recadero.orders.service.OrderValidationService;
import org.sirantar.recadero.orders.service.dto.InitiateReturnResponse;
import org.sirantar.recadero.users.repository.AddressRepository;

/**
 * End-to-end workflow test for the Orders module: create (from a completed
 * cart checkout) → confirm → ship → deliver → return, exercising the
 * service layer with mocked persistence (mirrors the other modules'
 * *IntegrationTest style).
 */
@DisplayName("Orders Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class OrderIntegrationTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private OrderPaymentRepository orderPaymentRepository;
  @Mock private OrderTransactionRepository orderTransactionRepository;
  @Mock private OrderShipmentRepository orderShipmentRepository;
  @Mock private OrderShipmentEventRepository orderShipmentEventRepository;
  @Mock private OrderReturnRepository orderReturnRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private AddressRepository addressRepository;
  @Mock private OrderEventPublisher eventPublisher;

  private OrderService orderService;
  private OrderShipmentService shipmentService;
  private OrderReturnService returnService;

  private Order order;

  @BeforeEach
  void setUp() {
    OrderValidationService validationService = new OrderValidationService();
    orderService = new OrderService(
        orderRepository, orderItemRepository, orderPaymentRepository, orderTransactionRepository,
        orderShipmentRepository, productVariantRepository, inventoryService, addressRepository,
        validationService, eventPublisher);
    shipmentService = new OrderShipmentService(orderShipmentRepository, orderShipmentEventRepository, orderRepository, eventPublisher);
    returnService = new OrderReturnService(orderReturnRepository, orderItemRepository);

    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
      Order o = inv.getArgument(0);
      if (o.getId() == null) {
        o.setId(1L);
        order = o;
      }
      return o;
    });
    when(inventoryService.checkAvailability(50L, 1)).thenReturn(5);
    when(productVariantRepository.findById(50L)).thenReturn(Optional.empty());
    when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(inv -> {
      OrderPayment p = inv.getArgument(0);
      if (p.getId() == null) p.setId(9L);
      return p;
    });
  }

  @Test
  @DisplayName("Should create order from checkout, ship it, mark delivered, and accept a return")
  void createConfirmShipDeliverReturnFlow() {
    CheckoutCompletedEvent event = new CheckoutCompletedEvent(
        1L, 10L,
        List.of(new CheckoutCompletedEvent.LineItem(50L, 1, BigDecimal.valueOf(19.99), BigDecimal.ZERO)),
        1L, 2L, "STANDARD", "CREDIT_CARD", "txn-1", BigDecimal.valueOf(19.99), "checkout-token", null, null);

    Order created = orderService.createOrderFromCart(event);
    assertThat(created.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

    // Ship
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderShipmentRepository.save(any(OrderShipment.class))).thenAnswer(inv -> {
      OrderShipment s = inv.getArgument(0);
      if (s.getId() == null) s.setId(7L);
      return s;
    });

    shipmentService.createShipment(1L, "FEDEX", "TRACK-XYZ", null);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

    // Deliver via carrier webhook
    OrderShipment shipment = new OrderShipment();
    shipment.setId(7L);
    shipment.setOrderId(1L);
    shipment.setTrackingNumber("TRACK-XYZ");
    shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
    when(orderShipmentRepository.findByTrackingNumber("TRACK-XYZ")).thenReturn(Optional.of(shipment));
    when(orderShipmentRepository.save(any(OrderShipment.class))).thenAnswer(inv -> inv.getArgument(0));

    shipmentService.updateShipmentStatus("TRACK-XYZ", "DELIVERED", "Front door");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    assertThat(order.getDeliveredDate()).isNotNull();

    // Initiate a return on the delivered order's item
    OrderItem item = new OrderItem();
    item.setId(500L);
    item.setOrderId(1L);
    item.setLineTotal(BigDecimal.valueOf(19.99));
    when(orderItemRepository.findById(500L)).thenReturn(Optional.of(item));
    when(orderReturnRepository.save(any())).thenAnswer(inv -> {
      var r = inv.getArgument(0, org.sirantar.recadero.orders.domain.OrderReturn.class);
      if (r.getId() == null) r.setId(1L);
      return r;
    });

    InitiateReturnResponse returnResponse = returnService.initiateReturn(1L, 500L, "DEFECTIVE", "cracked");
    assertThat(returnResponse.status()).isEqualTo("PENDING_APPROVAL");
    assertThat(returnResponse.orderId()).isEqualTo(1L);
  }
}
