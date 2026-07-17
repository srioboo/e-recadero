package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/orders (customer's own order history).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderListItem(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("order_number") String orderNumber,
    String status,
    @JsonProperty("grand_total") BigDecimal grandTotal,
    @JsonProperty("items_count") int itemsCount,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("confirmed_date") LocalDateTime confirmedDate,
    @JsonProperty("estimated_delivery") LocalDateTime estimatedDelivery) {}
