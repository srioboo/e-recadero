package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/orders/{order_id}/returns.
 */
public record ReturnListItem(
    @JsonProperty("return_id") Long returnId,
    @JsonProperty("order_id") Long orderId,
    String status,
    String reason,
    @JsonProperty("items_count") int itemsCount,
    @JsonProperty("estimated_refund") BigDecimal estimatedRefund,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
