package org.sirantar.recadero.catalog.events;

import java.util.UUID;

/**
 * Published when inventory is adjusted (reservations, stock changes, adjustments).
 */
public record InventoryUpdatedEvent(
    Long variantId,
    UUID warehouseId,
    int quantityChange,
    String reason
) {}

