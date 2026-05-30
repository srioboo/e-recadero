package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;

/**
 * Request payload for creating products.
 */
public record ProductCreateRequest(
    Long categoryId,
    String sku,
    String name,
    String description,
    String shortDescription,
    BigDecimal basePrice,
    BigDecimal costPrice,
    Boolean featured,
    String status) {}
