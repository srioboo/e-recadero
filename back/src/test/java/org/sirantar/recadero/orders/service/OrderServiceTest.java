package org.sirantar.recadero.orders.service;

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
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.PaymentStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderPaymentRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.repository.OrderTransactionRepository;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.users.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private OrderPaymentRepository orderPaymentRepository;
  @Mock private OrderTransactionRepository orderTransactionRepository;
  @Mock private OrderShipmentRepository orderShipmentRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private AddressRepository addressRepository;
  @Mock private OrderEventPublisher eventPublisher;

  private final OrderValidationService orderValidationService = new OrderValidationService();
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    orderService = new OrderService(
        orderRepository, orderItemRepository, orderPaymentRepository, orderTransactionRepository,
        orderShipmentRepository, productVariantRepository, inventoryService, addressRepository,
        orderValidationService, eventPublisher);
  }

  @Test
  void createOrderFromCartChecksInventoryAvailabilityForEachItem() {
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
      Order o = inv.getArgument(0);
      if (o.getId() == null) o.setId(1L);
      return o;
    });
    when(inventoryService.checkAvailability(50L, 2)).thenReturn(10);
    when(productVariantRepository.findById(50L)).thenReturn(Optional.empty());
    when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(inv -> {
      OrderPayment p = inv.getArgument(0);
      if (p.getId() == null) p.setId(9L);
      return p;
    });

    CheckoutCompletedEvent event = new CheckoutCompletedEvent(
        1L, 10L,
        List.of(new CheckoutCompletedEvent.LineItem(50L, 2, BigDecimal.valueOf(20), BigDecimal.ZERO)),
        1L, 2L, "STANDARD", "CREDIT_CARD", "txn-1", BigDecimal.valueOf(40), "checkout-token", null, null);

    Order created = orderService.createOrderFromCart(event);

    assertThat(created.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(created.getGrandTotal()).isEqualByComparingTo("40");
    org.mockito.Mockito.verify(inventoryService).checkAvailability(50L, 2);
    org.mockito.Mockito.verify(eventPublisher).publishConfirmed(
        org.mockito.ArgumentMatchers.eq(1L), any(), org.mockito.ArgumentMatchers.eq(10L), any(), any(), any());
  }

  @Test
  void cancelOrderRejectsAlreadyShippedOrder() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setStatus(OrderStatus.SHIPPED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L, "changed my mind"))
        .isInstanceOf(ResourceConflictException.class)
        .satisfies(ex -> assertThat(((ResourceConflictException) ex).getErrorCode()).isEqualTo("CANNOT_CANCEL"));
  }

  @Test
  void cancelOrderSucceedsForPendingOrder() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setStatus(OrderStatus.PENDING);
    order.setGrandTotal(BigDecimal.valueOf(40));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

    var response = orderService.cancelOrder(1L, 10L, "changed my mind");

    assertThat(response.status()).isEqualTo("CANCELLED");
    assertThat(response.refundInitiated()).isFalse();
  }

  @Test
  void refundOrderRejectsOutsideThirtyDayWindow() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveredDate(LocalDateTime.now().minusDays(45));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.refundOrder(1L, 10L, null, "too late"))
        .isInstanceOf(ResourceConflictException.class)
        .satisfies(ex -> assertThat(((ResourceConflictException) ex).getErrorCode()).isEqualTo("REFUND_WINDOW_CLOSED"));
  }

  @Test
  void refundOrderSucceedsWithinWindow() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveredDate(LocalDateTime.now().minusDays(5));
    order.setGrandTotal(BigDecimal.valueOf(40));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderPayment payment = new OrderPayment();
    payment.setId(9L);
    payment.setOrderId(1L);
    payment.setAmount(BigDecimal.valueOf(40));
    payment.setStatus(PaymentStatus.CAPTURED);
    when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
    when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(orderTransactionRepository.save(any())).thenAnswer(inv -> {
      var t = inv.getArgument(0, org.sirantar.recadero.orders.domain.OrderTransaction.class);
      t.setId(99L);
      return t;
    });

    var response = orderService.refundOrder(1L, 10L, BigDecimal.valueOf(40), "defective");

    assertThat(response.status()).isEqualTo("INITIATED");
    assertThat(response.amount()).isEqualByComparingTo("40");
  }
}
