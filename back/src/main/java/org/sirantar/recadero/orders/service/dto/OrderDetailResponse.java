package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for GET /api/v1/orders/{order_id}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderDetailResponse(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("order_number") String orderNumber,
    @JsonProperty("user_id") Long userId,
    String status,
    List<OrderItemDetail> items,
    OrderCalculations calculations,
    Addresses addresses,
    PaymentSummary payment,
    ShipmentSummary shipment,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("confirmed_date") LocalDateTime confirmedDate,
    @JsonProperty("updated_at") LocalDateTime updatedAt) {

  public record Addresses(AddressSnapshot billing, AddressSnapshot shipping) {}
}
