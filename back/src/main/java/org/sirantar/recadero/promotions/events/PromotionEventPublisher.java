package org.sirantar.recadero.promotions.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for the Promotions module's domain events.
 */
@Component
@RequiredArgsConstructor
public class PromotionEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishActivated(Long promotionId, String promotionName, LocalDateTime startDate) {
    eventPublisher.publishEvent(new PromotionActivatedEvent(promotionId, promotionName, startDate));
  }

  public void publishExpired(Long promotionId, String promotionName, int totalUsage, BigDecimal totalDiscount) {
    eventPublisher.publishEvent(new PromotionExpiredEvent(promotionId, promotionName, totalUsage, totalDiscount));
  }

  public void publishCouponUsed(String couponCode, Long promotionId, Long orderId, Long userId, BigDecimal discountAmount) {
    eventPublisher.publishEvent(new CouponUsedEvent(couponCode, promotionId, orderId, userId, discountAmount));
  }
}
