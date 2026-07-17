package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for POST /api/v1/orders/{order_id}/return.
 */
public record InitiateReturnResponse(
    @JsonProperty("return_id") Long returnId,
    @JsonProperty("order_id") Long orderId,
    String status,
    String reason,
    @JsonProperty("return_tracking_number") String returnTrackingNumber,
    int items,
    @JsonProperty("estimated_refund") BigDecimal estimatedRefund,
    @JsonProperty("next_steps") String nextSteps,
    @JsonProperty("return_shipping_label") String returnShippingLabel) {}
