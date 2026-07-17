package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for GET /api/v1/orders/{order_id}/shipment.
 */
public record ShipmentDetailResponse(
    @JsonProperty("shipment_id") Long shipmentId,
    @JsonProperty("order_id") Long orderId,
    String carrier,
    @JsonProperty("tracking_number") String trackingNumber,
    String status,
    @JsonProperty("shipped_at") LocalDateTime shippedAt,
    @JsonProperty("estimated_delivery") LocalDateTime estimatedDelivery,
    @JsonProperty("delivered_at") LocalDateTime deliveredAt,
    @JsonProperty("tracking_history") List<TrackingEvent> trackingHistory) {

  public record TrackingEvent(LocalDateTime timestamp, String status, String location) {}
}
