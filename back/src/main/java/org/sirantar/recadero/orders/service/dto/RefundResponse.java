package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for POST /api/v1/orders/{order_id}/refund.
 */
public record RefundResponse(
    @JsonProperty("refund_id") Long refundId,
    @JsonProperty("order_id") Long orderId,
    BigDecimal amount,
    String status,
    String reason,
    @JsonProperty("estimated_completion") LocalDateTime estimatedCompletion,
    String message) {}
