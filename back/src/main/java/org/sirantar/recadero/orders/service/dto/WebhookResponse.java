package org.sirantar.recadero.orders.service.dto;

/**
 * Response payload for the shipment carrier webhook.
 */
public record WebhookResponse(boolean received, String message) {}
