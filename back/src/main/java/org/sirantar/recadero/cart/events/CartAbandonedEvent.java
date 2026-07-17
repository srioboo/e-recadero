package org.sirantar.recadero.cart.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published when a cart expires without checkout (24h inactivity). An Email
 * Service module (not yet implemented) is expected to subscribe for recovery emails.
 */
public record CartAbandonedEvent(
    Long cartId, Long userId, int itemsCount, BigDecimal grandTotal, LocalDateTime abandonedAt) {}
