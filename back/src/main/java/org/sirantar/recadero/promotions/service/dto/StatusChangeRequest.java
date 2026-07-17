package org.sirantar.recadero.promotions.service.dto;

/**
 * Request payload for PATCH /api/v1/promotions/{id}/status.
 */
public record StatusChangeRequest(String status, String reason) {}
