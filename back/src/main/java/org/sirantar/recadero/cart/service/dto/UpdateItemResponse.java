package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for PUT /api/v1/cart/items/{cart_item_id}.
 */
public record UpdateItemResponse(
    @JsonProperty("cart_item_id") Long cartItemId,
    int quantity,
    @JsonProperty("line_total") BigDecimal lineTotal,
    @JsonProperty("cart_updated") CartUpdatedSummary cartUpdated) {}
