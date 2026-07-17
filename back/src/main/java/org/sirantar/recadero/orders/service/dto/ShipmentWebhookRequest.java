package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/orders/{order_id}/shipment/webhook (carrier callback).
 */
public record ShipmentWebhookRequest(
    @JsonProperty("tracking_number") String trackingNumber,
    String status,
    String timestamp,
    String location,
    @JsonProperty("signature_required") Boolean signatureRequired,
    @JsonProperty("signature_url") String signatureUrl) {}
