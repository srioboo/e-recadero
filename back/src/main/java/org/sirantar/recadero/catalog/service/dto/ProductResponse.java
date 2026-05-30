package org.sirantar.recadero.catalog.service.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product view model.
 */
public record ProductResponse(
    Long id,
    Long categoryId,
    String sku,
    String name,
    String description,
    String shortDescription,
    BigDecimal basePrice,
    BigDecimal costPrice,
    String status,
    Boolean featured,
    List<ProductVariantResponse> variants,
    List<String> images) {}
