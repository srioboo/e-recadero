package org.sirantar.recadero.cart.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Published when checkout is confirmed (payment succeeded). Carries a
 * full line-item snapshot so a future Orders module (Phase 6 — not yet
 * implemented) can create the actual order purely by subscribing to this
 * event, without Cart needing a synchronous dependency on Orders.
 */
public record CheckoutCompletedEvent(
    Long cartId,
    Long userId,
    List<LineItem> items,
    Long billingAddressId,
    Long shippingAddressId,
    String shippingMethodId,
    String paymentMethod,
    String transactionId,
    BigDecimal grandTotal,
    String checkoutToken) {

  public record LineItem(Long productVariantId, int quantity, BigDecimal priceAtTime, BigDecimal discountApplied) {}
}
