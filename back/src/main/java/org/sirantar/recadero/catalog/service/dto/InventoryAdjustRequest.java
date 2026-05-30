package org.sirantar.recadero.catalog.service.dto;

/**
 * Request payload for inventory adjustments.
 */
public record InventoryAdjustRequest(int quantityChange, String reason, String notes) {}
