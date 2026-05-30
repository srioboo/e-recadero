package org.sirantar.recadero.catalog.service.dto;

import java.time.LocalDateTime;

/**
 * Inventory view model.
 */
public record InventoryResponse(
    Long productVariantId,
    Integer quantityOnHand,
    Integer reservedQuantity,
    Integer availableQuantity,
    Integer reorderLevel,
    LocalDateTime lastCountedAt) {}
