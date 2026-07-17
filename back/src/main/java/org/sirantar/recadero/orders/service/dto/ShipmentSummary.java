package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Abbreviated shipment info embedded in an order detail response.
 */
public record ShipmentSummary(
    String carrier,
    String method,
    @JsonProperty("tracking_number") String trackingNumber,
    String status,
    @JsonProperty("shipped_at") LocalDateTime shippedAt,
    @JsonProperty("estimated_delivery") LocalDateTime estimatedDelivery) {}
