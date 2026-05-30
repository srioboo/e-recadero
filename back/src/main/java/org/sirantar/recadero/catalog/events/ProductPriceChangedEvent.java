package org.sirantar.recadero.catalog.events;

import java.math.BigDecimal;

/**
 * Published when a product price changes.
 */
public record ProductPriceChangedEvent(Long productId, BigDecimal oldPrice, BigDecimal newPrice) {}
