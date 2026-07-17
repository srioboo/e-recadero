package org.sirantar.recadero.orders.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for the Orders module's domain events.
 */
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishConfirmed(
      Long orderId, String orderNumber, Long userId, BigDecimal grandTotal, String couponCode, BigDecimal discountAmount) {
    eventPublisher.publishEvent(new OrderConfirmedEvent(orderId, orderNumber, userId, grandTotal, couponCode, discountAmount));
  }

  public void publishShipped(
      Long orderId, String orderNumber, Long userId, String trackingNumber, String carrier, LocalDateTime estimatedDelivery) {
    eventPublisher.publishEvent(
        new OrderShippedEvent(orderId, orderNumber, userId, trackingNumber, carrier, estimatedDelivery));
  }

  public void publishDelivered(Long orderId, String orderNumber, Long userId, LocalDateTime deliveredAt) {
    eventPublisher.publishEvent(new OrderDeliveredEvent(orderId, orderNumber, userId, deliveredAt));
  }

  public void publishRefunded(Long orderId, BigDecimal refundAmount, String reason) {
    eventPublisher.publishEvent(new OrderRefundedEvent(orderId, refundAmount, reason));
  }
}
