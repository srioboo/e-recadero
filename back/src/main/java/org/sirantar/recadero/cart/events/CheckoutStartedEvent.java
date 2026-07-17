package org.sirantar.recadero.cart.events;

import java.math.BigDecimal;

/**
 * Published when a cart transitions to checkout (inventory locked).
 */
public record CheckoutStartedEvent(Long cartId, Long userId, BigDecimal grandTotal, String checkoutToken) {}
