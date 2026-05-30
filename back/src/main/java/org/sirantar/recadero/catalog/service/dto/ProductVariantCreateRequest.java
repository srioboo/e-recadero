package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;

/**
 * Request payload for creating a product variant.
 */
public record ProductVariantCreateRequest(
    String sku, String variantAttributes, BigDecimal price, BigDecimal weight) {}
