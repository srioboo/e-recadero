package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;

/**
 * Request payload for updating product price.
 */
public record ProductPriceUpdateRequest(BigDecimal price) {}
