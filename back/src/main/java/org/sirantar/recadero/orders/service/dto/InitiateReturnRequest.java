package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/orders/{order_id}/return.
 */
public record InitiateReturnRequest(
    @JsonProperty("order_item_id") Long orderItemId, String reason, String description) {}
