package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;

/**
 * Request payload for updating a product variant.
 */
public record ProductVariantUpdateRequest(
    String sku, String variantAttributes, BigDecimal price, BigDecimal weight) {}
