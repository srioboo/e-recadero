package org.sirantar.recadero.cart.events;

import java.math.BigDecimal;

/**
 * Published when an item is added to a cart.
 */
public record CartItemAddedEvent(
    Long cartId, Long userId, Long productVariantId, int quantity, BigDecimal price) {}
