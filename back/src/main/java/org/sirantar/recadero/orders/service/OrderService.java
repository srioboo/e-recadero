package org.sirantar.recadero.orders.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.OrderTransaction;
import org.sirantar.recadero.orders.domain.PaymentStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderPaymentRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.repository.OrderTransactionRepository;
import org.sirantar.recadero.orders.service.dto.AddressSnapshot;
import org.sirantar.recadero.orders.service.dto.AdminOrderListItem;
import org.sirantar.recadero.orders.service.dto.CancelOrderResponse;
import org.sirantar.recadero.orders.service.dto.OrderCalculations;
import org.sirantar.recadero.orders.service.dto.OrderDetailResponse;
import org.sirantar.recadero.orders.service.dto.OrderItemDetail;
import org.sirantar.recadero.orders.service.dto.OrderListItem;
import org.sirantar.recadero.orders.service.dto.PaymentDetailResponse;
import org.sirantar.recadero.orders.service.dto.PaymentSummary;
import org.sirantar.recadero.orders.service.dto.RefundResponse;
import org.sirantar.recadero.orders.service.dto.ShipmentSummary;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.users.domain.Address;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order lifecycle: creation from a completed cart checkout, retrieval,
 * cancellation, and refunds.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderPaymentRepository orderPaymentRepository;
  private final OrderTransactionRepository orderTransactionRepository;
  private final OrderShipmentRepository orderShipmentRepository;
  private final ProductVariantRepository productVariantRepository;
  private final InventoryService inventoryService;
  private final AddressRepository addressRepository;
  private final OrderValidationService orderValidationService;
  private final OrderEventPublisher eventPublisher;

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Order createOrderFromCart(CheckoutCompletedEvent event) {
    for (CheckoutCompletedEvent.LineItem item : event.items()) {
      int available = inventoryService.checkAvailability(item.productVariantId(), item.quantity());
      if (available < 0) {
        log.warn("Variant {} shows negative availability ({}) when creating order for cart {}",
            item.productVariantId(), available, event.cartId());
      }
    }

    LocalDateTime now = LocalDateTime.now();
    Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setUserId(event.userId());
    order.setStatus(OrderStatus.CONFIRMED);
    order.setBillingAddressId(event.billingAddressId());
    order.setShippingAddressId(event.shippingAddressId());
    order.setShippingMethodId(event.shippingMethodId());
    order.setOrderDate(now);
    order.setConfirmedDate(now);
    order.setUpdatedAt(now);

    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal discountTotal = BigDecimal.ZERO;
    for (CheckoutCompletedEvent.LineItem item : event.items()) {
      subtotal = subtotal.add(item.priceAtTime().multiply(BigDecimal.valueOf(item.quantity())));
      discountTotal = discountTotal.add(item.discountApplied() != null ? item.discountApplied() : BigDecimal.ZERO);
    }
    order.setSubtotal(subtotal);
    order.setDiscountTotal(discountTotal);
    order.setTaxTotal(BigDecimal.ZERO);
    order.setShippingTotal(BigDecimal.ZERO);
    order.setGrandTotal(event.grandTotal() != null ? event.grandTotal() : subtotal.subtract(discountTotal));

    Order savedOrder = orderRepository.save(order);

    for (CheckoutCompletedEvent.LineItem item : event.items()) {
      ProductVariant variant = productVariantRepository.findById(item.productVariantId()).orElse(null);
      OrderItem orderItem = new OrderItem();
      orderItem.setOrderId(savedOrder.getId());
      orderItem.setProductVariantId(item.productVariantId());
      orderItem.setProductSku(variant != null ? variant.getSku() : null);
      orderItem.setProductName(variant != null ? variant.getProduct().getName() : null);
      orderItem.setQuantity(item.quantity());
      orderItem.setUnitPrice(item.priceAtTime());
      orderItem.setLineDiscount(item.discountApplied() != null ? item.discountApplied() : BigDecimal.ZERO);
      orderItem.setLineTotal(item.priceAtTime().multiply(BigDecimal.valueOf(item.quantity()))
          .subtract(item.discountApplied() != null ? item.discountApplied() : BigDecimal.ZERO));
      orderItem.setCreatedAt(now);
      orderItemRepository.save(orderItem);
    }

    OrderPayment payment = new OrderPayment();
    payment.setOrderId(savedOrder.getId());
    payment.setPaymentMethod(event.paymentMethod());
    payment.setTransactionId(event.transactionId());
    payment.setAmount(savedOrder.getGrandTotal());
    payment.setStatus(PaymentStatus.CAPTURED);
    payment.setPaidAt(now);
    payment.setCreatedAt(now);
    payment.setUpdatedAt(now);
    OrderPayment savedPayment = orderPaymentRepository.save(payment);

    OrderTransaction transaction = new OrderTransaction();
    transaction.setOrderId(savedOrder.getId());
    transaction.setPaymentId(savedPayment.getId());
    transaction.setTransactionType("CAPTURE");
    transaction.setAmount(savedOrder.getGrandTotal());
    transaction.setCreatedAt(now);
    orderTransactionRepository.save(transaction);

    eventPublisher.publishConfirmed(
        savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getUserId(), savedOrder.getGrandTotal(),
        event.couponCode(), event.discountAmount());
    log.info("Created order {} from cart {}", savedOrder.getOrderNumber(), event.cartId());

    return savedOrder;
  }

  public OrderDetailResponse getOrder(Long orderId, Long userId) {
    Order order = getOwnedOrder(orderId, userId);
    return toDetail(order);
  }

  public PaymentDetailResponse getPaymentDetail(Long orderId, Long userId) {
    getOwnedOrder(orderId, userId);
    OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("No payment found for order: " + orderId));

    List<OrderTransaction> refunds = orderTransactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
        .filter(t -> "REFUND".equals(t.getTransactionType()))
        .toList();
    List<PaymentDetailResponse.RefundHistoryEntry> refundHistory = refunds.stream()
        .map(t -> new PaymentDetailResponse.RefundHistoryEntry(t.getAmount(), t.getCreatedAt(), t.getResponseMessage()))
        .toList();
    String refundStatus = refundHistory.isEmpty() ? "NO_REFUND" : payment.getStatus().name();

    return new PaymentDetailResponse(
        payment.getId(),
        orderId,
        payment.getStatus().name(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getPaymentMethod(),
        payment.getTransactionId(),
        payment.getPaidAt(),
        refundStatus,
        refundHistory);
  }

  public PaginationResponse<OrderListItem> listOrders(Long userId, OrderStatus status, Pageable pageable) {
    Page<Order> page = status != null
        ? orderRepository.search(userId, status, null, null, null, null, pageable)
        : orderRepository.findByUserId(userId, pageable);
    return PaginationResponse.from(page.map(this::toListItem));
  }

  public PaginationResponse<AdminOrderListItem> listOrdersAdmin(
      Long userId, OrderStatus status, LocalDateTime fromDate, LocalDateTime toDate,
      BigDecimal minTotal, BigDecimal maxTotal, Pageable pageable) {
    Page<Order> page = orderRepository.search(userId, status, fromDate, toDate, minTotal, maxTotal, pageable);
    return PaginationResponse.from(page.map(this::toAdminListItem));
  }

  @Transactional
  public CancelOrderResponse cancelOrder(Long orderId, Long userId, String reason) {
    Order order = getOwnedOrder(orderId, userId);
    orderValidationService.validateCancellable(order.getStatus());

    order.setStatus(OrderStatus.CANCELLED);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    boolean refundInitiated = false;
    OrderPayment payment = orderPaymentRepository.findByOrderId(orderId).orElse(null);
    LocalDateTime estimatedRefundDate = null;
    if (payment != null && payment.getStatus() == PaymentStatus.CAPTURED) {
      refundInitiated = true;
      estimatedRefundDate = LocalDateTime.now().plusDays(5);
      recordRefund(order, payment, order.getGrandTotal(), reason);
    }

    return new CancelOrderResponse(
        order.getId(), order.getStatus().name(), "Order cancelled successfully", refundInitiated, estimatedRefundDate);
  }

  @Transactional
  public RefundResponse refundOrder(Long orderId, Long userId, BigDecimal amount, String reason) {
    Order order = getOwnedOrder(orderId, userId);
    orderValidationService.validateWithinRefundWindow(order.getDeliveredDate());

    OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("No payment found for order: " + orderId));
    BigDecimal refundAmount = amount != null ? amount : order.getGrandTotal();

    OrderTransaction transaction = recordRefund(order, payment, refundAmount, reason);
    eventPublisher.publishRefunded(order.getId(), refundAmount, reason);

    return new RefundResponse(
        transaction.getId(),
        order.getId(),
        refundAmount,
        "INITIATED",
        reason,
        LocalDateTime.now().plusDays(5),
        "Refund initiated. Check your account in 3-5 business days.");
  }

  @Transactional
  public OrderDetailResponse adminChangeStatus(Long orderId, OrderStatus status, String notes) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    orderValidationService.validateTransition(order.getStatus(), status);

    order.setStatus(status);
    order.setUpdatedAt(LocalDateTime.now());
    if (status == OrderStatus.SHIPPED) {
      order.setShippedDate(LocalDateTime.now());
    } else if (status == OrderStatus.DELIVERED) {
      order.setDeliveredDate(LocalDateTime.now());
    }
    orderRepository.save(order);
    return toDetail(order);
  }

  private OrderTransaction recordRefund(Order order, OrderPayment payment, BigDecimal amount, String reason) {
    LocalDateTime now = LocalDateTime.now();
    payment.setStatus(
        amount.compareTo(payment.getAmount()) >= 0 ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
    payment.setRefundedAt(now);
    payment.setUpdatedAt(now);
    orderPaymentRepository.save(payment);

    OrderTransaction transaction = new OrderTransaction();
    transaction.setOrderId(order.getId());
    transaction.setPaymentId(payment.getId());
    transaction.setTransactionType("REFUND");
    transaction.setAmount(amount);
    transaction.setResponseMessage(reason);
    transaction.setCreatedAt(now);
    return orderTransactionRepository.save(transaction);
  }

  private Order getOwnedOrder(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    if (!order.getUserId().equals(userId)) {
      throw new ResourceNotFoundException("Order not found: " + orderId);
    }
    return order;
  }

  private String generateOrderNumber() {
    String datePart = LocalDateTime.now().format(ORDER_NUMBER_DATE);
    String randomPart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    return "ORD-" + datePart + "-" + randomPart;
  }

  private OrderListItem toListItem(Order order) {
    return new OrderListItem(
        order.getId(),
        order.getOrderNumber(),
        order.getStatus().name(),
        order.getGrandTotal(),
        orderItemRepository.countByOrderId(order.getId()),
        order.getOrderDate(),
        order.getConfirmedDate(),
        orderShipmentRepository.findByOrderId(order.getId())
            .map(s -> s.getEstimatedDeliveryDate() != null ? s.getEstimatedDeliveryDate().atTime(23, 59, 59) : null)
            .orElse(null));
  }

  private AdminOrderListItem toAdminListItem(Order order) {
    return new AdminOrderListItem(
        order.getId(),
        order.getOrderNumber(),
        order.getUserId(),
        order.getStatus().name(),
        order.getGrandTotal(),
        orderItemRepository.countByOrderId(order.getId()),
        order.getOrderDate(),
        orderShipmentRepository.findByOrderId(order.getId()).map(s -> s.getStatus().name()).orElse(null));
  }

  private OrderDetailResponse toDetail(Order order) {
    List<OrderItemDetail> items = orderItemRepository.findByOrderId(order.getId()).stream()
        .map(i -> new OrderItemDetail(
            i.getId(), i.getProductName(), i.getProductSku(), i.getQuantity(), i.getUnitPrice(), i.getLineDiscount(), i.getLineTotal()))
        .toList();

    OrderCalculations calculations = new OrderCalculations(
        order.getSubtotal(), order.getTaxTotal(), order.getShippingTotal(), order.getDiscountTotal(), order.getGrandTotal());

    OrderDetailResponse.Addresses addresses = new OrderDetailResponse.Addresses(
        resolveAddress(order.getBillingAddressId()), resolveAddress(order.getShippingAddressId()));

    PaymentSummary payment = orderPaymentRepository.findByOrderId(order.getId())
        .map(p -> new PaymentSummary(p.getStatus().name(), p.getPaymentMethod(), p.getTransactionId(), p.getAmount(), p.getPaidAt()))
        .orElse(null);

    ShipmentSummary shipment = orderShipmentRepository.findByOrderId(order.getId())
        .map(s -> new ShipmentSummary(
            s.getCarrier(),
            order.getShippingMethodId(),
            s.getTrackingNumber(),
            s.getStatus().name(),
            s.getShippedAt(),
            s.getEstimatedDeliveryDate() != null ? s.getEstimatedDeliveryDate().atTime(23, 59, 59) : null))
        .orElse(null);

    return new OrderDetailResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getUserId(),
        order.getStatus().name(),
        items,
        calculations,
        addresses,
        payment,
        shipment,
        order.getOrderDate(),
        order.getConfirmedDate(),
        order.getUpdatedAt());
  }

  private AddressSnapshot resolveAddress(Long addressId) {
    if (addressId == null) {
      return null;
    }
    return addressRepository.findById(addressId)
        .map(a -> new AddressSnapshot(a.getStreetAddress(), a.getCity(), a.getStateProvince(), a.getPostalCode(), a.getCountryCode()))
        .orElse(null);
  }
}
