package org.sirantar.recadero.promotions.service;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.orders.events.OrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Subscribes to Orders' {@link OrderConfirmedEvent} to record real coupon
 * usage (increment counters, create a {@code PromotionUsage} row) once an
 * order genuinely exists — this keeps Orders unaware of Promotions
 * entirely (no reverse dependency), mirroring how Orders itself listens
 * to Cart's CheckoutCompletedEvent.
 */
@Component
@RequiredArgsConstructor
public class OrderConfirmedEventListener {

  private static final Logger log = LoggerFactory.getLogger(OrderConfirmedEventListener.class);

  private final CouponCodeService couponCodeService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderConfirmed(OrderConfirmedEvent event) {
    if (event.couponCode() == null || event.couponCode().isBlank()) {
      return;
    }
    try {
      couponCodeService.applyCoupon(event.couponCode(), event.orderId(), event.userId(), event.discountAmount());
    } catch (RuntimeException e) {
      log.warn("Failed to record coupon usage for order {} (coupon {}): {}", event.orderId(), event.couponCode(), e.getMessage());
    }
  }
}
