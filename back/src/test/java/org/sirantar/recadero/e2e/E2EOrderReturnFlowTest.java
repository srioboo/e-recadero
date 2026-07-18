package org.sirantar.recadero.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.PaymentStatus;
import org.sirantar.recadero.orders.domain.ReturnStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderPaymentRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderReturnRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.repository.OrderTransactionRepository;
import org.sirantar.recadero.orders.service.OrderReturnService;
import org.sirantar.recadero.orders.service.OrderService;
import org.sirantar.recadero.orders.service.OrderValidationService;
import org.sirantar.recadero.orders.service.dto.InitiateReturnResponse;
import org.sirantar.recadero.orders.service.dto.RefundResponse;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.sirantar.recadero.catalog.service.InventoryService;

/**
 * Order return/refund journey (T181): a delivered order's item is
 * returned, approved by an admin, and refunded — verifying the refund is
 * recorded against the original payment.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("E2E: order delivered -> return -> approve -> refund")
class E2EOrderReturnFlowTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private OrderPaymentRepository orderPaymentRepository;
  @Mock private OrderTransactionRepository orderTransactionRepository;
  @Mock private OrderShipmentRepository orderShipmentRepository;
  @Mock private OrderReturnRepository orderReturnRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private AddressRepository addressRepository;
  @Mock private OrderEventPublisher eventPublisher;

  private OrderService orderService;
  private OrderReturnService orderReturnService;

  private Order deliveredOrder;
  private OrderItem item;

  @BeforeEach
  void setUp() {
    orderService = new OrderService(
        orderRepository, orderItemRepository, orderPaymentRepository, orderTransactionRepository,
        orderShipmentRepository, productVariantRepository, inventoryService, addressRepository,
        new OrderValidationService(), eventPublisher);
    orderReturnService = new OrderReturnService(orderReturnRepository, orderItemRepository);

    deliveredOrder = new Order();
    deliveredOrder.setId(900L);
    deliveredOrder.setUserId(10L);
    deliveredOrder.setOrderNumber("ORD-20260716-ABC123");
    deliveredOrder.setStatus(OrderStatus.DELIVERED);
    deliveredOrder.setGrandTotal(BigDecimal.valueOf(72.00));
    deliveredOrder.setDeliveredDate(LocalDateTime.now().minusDays(3));

    item = new OrderItem();
    item.setId(500L);
    item.setOrderId(900L);
    item.setProductName("Wireless Mouse");
    item.setLineTotal(BigDecimal.valueOf(72.00));
  }

  @Test
  @DisplayName("delivered order item is returned, approved, and refunded against the original payment")
  void orderReturnAndRefundFlow() {
    when(orderItemRepository.findById(500L)).thenReturn(Optional.of(item));
    when(orderReturnRepository.save(any())).thenAnswer(inv -> {
      var r = inv.getArgument(0, org.sirantar.recadero.orders.domain.OrderReturn.class);
      if (r.getId() == null) r.setId(1L);
      return r;
    });

    InitiateReturnResponse initiated = orderReturnService.initiateReturn(900L, 500L, "DEFECTIVE", "Scroll wheel stuck");
    assertThat(initiated.status()).isEqualTo("PENDING_APPROVAL");
    assertThat(initiated.returnTrackingNumber()).startsWith("RMA-");

    when(orderReturnRepository.findById(1L)).thenAnswer(inv -> {
      var r = new org.sirantar.recadero.orders.domain.OrderReturn();
      r.setId(1L);
      r.setOrderId(900L);
      r.setOrderItemId(500L);
      r.setStatus(ReturnStatus.PENDING_APPROVAL);
      r.setRefundAmount(BigDecimal.valueOf(72.00));
      r.setRequestedAt(LocalDateTime.now());
      return Optional.of(r);
    });

    var approved = orderReturnService.approveReturn(1L);
    assertThat(approved.getStatus()).isEqualTo(ReturnStatus.APPROVED);

    // Refund is processed against the order's original payment (independent of the return record).
    when(orderRepository.findById(900L)).thenReturn(Optional.of(deliveredOrder));
    OrderPayment payment = new OrderPayment();
    payment.setId(1L);
    payment.setOrderId(900L);
    payment.setAmount(BigDecimal.valueOf(72.00));
    payment.setStatus(PaymentStatus.CAPTURED);
    when(orderPaymentRepository.findByOrderId(900L)).thenReturn(Optional.of(payment));
    when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(orderTransactionRepository.save(any())).thenAnswer(inv -> {
      var t = inv.getArgument(0, org.sirantar.recadero.orders.domain.OrderTransaction.class);
      t.setId(1L);
      return t;
    });

    RefundResponse refund = orderService.refundOrder(900L, 10L, BigDecimal.valueOf(72.00), "Defective item returned");
    assertThat(refund.status()).isEqualTo("INITIATED");
    assertThat(refund.amount()).isEqualByComparingTo("72.00");
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

    var completedReturn = orderReturnService.processReturnRefund(1L, BigDecimal.valueOf(72.00));
    assertThat(completedReturn.getStatus()).isEqualTo(ReturnStatus.COMPLETED);
    assertThat(completedReturn.getCompletedAt()).isNotNull();
  }
}
