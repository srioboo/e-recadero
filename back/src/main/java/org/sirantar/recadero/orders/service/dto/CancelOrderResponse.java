package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for PATCH /api/v1/orders/{order_id}/cancel.
 */
public record CancelOrderResponse(
    @JsonProperty("order_id") Long orderId,
    String status,
    String message,
    @JsonProperty("refund_initiated") boolean refundInitiated,
    @JsonProperty("estimated_refund_date") LocalDateTime estimatedRefundDate) {}
