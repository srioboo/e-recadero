package org.sirantar.recadero.catalog.service.dto;

/**
 * Variant availability projection.
 */
public record ProductVariantAvailabilityResponse(
    Long variantId, Integer availableQuantity, Boolean isAvailable, String reorderStatus) {}
