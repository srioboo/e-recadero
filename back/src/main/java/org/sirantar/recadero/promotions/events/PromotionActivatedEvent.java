package org.sirantar.recadero.promotions.events;

import java.time.LocalDateTime;

/**
 * Published when a promotion becomes active.
 */
public record PromotionActivatedEvent(Long promotionId, String promotionName, LocalDateTime startDate) {}
