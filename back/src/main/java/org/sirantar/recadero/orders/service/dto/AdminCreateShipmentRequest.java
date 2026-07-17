package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Request payload for POST /api/v1/admin/orders/{order_id}/shipment.
 */
public record AdminCreateShipmentRequest(
    String carrier,
    @JsonProperty("tracking_number") String trackingNumber,
    @JsonProperty("estimated_delivery") LocalDateTime estimatedDelivery) {}
