package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/admin/orders.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminOrderListItem(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("order_number") String orderNumber,
    @JsonProperty("user_id") Long userId,
    String status,
    @JsonProperty("grand_total") BigDecimal grandTotal,
    @JsonProperty("items_count") int itemsCount,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("shipment_status") String shipmentStatus) {}
