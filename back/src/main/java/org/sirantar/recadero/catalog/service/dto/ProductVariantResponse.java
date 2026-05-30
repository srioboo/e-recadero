package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;

/**
 * Product variant view model.
 */
public record ProductVariantResponse(
    Long id, String sku, String variantAttributes, BigDecimal price, BigDecimal weight) {}
