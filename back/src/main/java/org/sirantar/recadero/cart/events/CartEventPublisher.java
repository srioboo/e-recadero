package org.sirantar.recadero.cart.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for the Cart module's domain events.
 */
@Component
@RequiredArgsConstructor
public class CartEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishItemAdded(Long cartId, Long userId, Long productVariantId, int quantity, BigDecimal price) {
    eventPublisher.publishEvent(new CartItemAddedEvent(cartId, userId, productVariantId, quantity, price));
  }

  public void publishAbandoned(Long cartId, Long userId, int itemsCount, BigDecimal grandTotal, LocalDateTime abandonedAt) {
    eventPublisher.publishEvent(new CartAbandonedEvent(cartId, userId, itemsCount, grandTotal, abandonedAt));
  }

  public void publishCheckoutStarted(Long cartId, Long userId, BigDecimal grandTotal, String checkoutToken) {
    eventPublisher.publishEvent(new CheckoutStartedEvent(cartId, userId, grandTotal, checkoutToken));
  }

  public void publishCheckoutCompleted(
      Long cartId,
      Long userId,
      List<CheckoutCompletedEvent.LineItem> items,
      Long billingAddressId,
      Long shippingAddressId,
      String shippingMethodId,
      String paymentMethod,
      String transactionId,
      BigDecimal grandTotal,
      String checkoutToken) {
    eventPublisher.publishEvent(new CheckoutCompletedEvent(
        cartId, userId, items, billingAddressId, shippingAddressId, shippingMethodId,
        paymentMethod, transactionId, grandTotal, checkoutToken));
  }
}
