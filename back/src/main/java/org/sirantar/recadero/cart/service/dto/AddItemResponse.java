package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for POST /api/v1/cart/items.
 */
public record AddItemResponse(
    @JsonProperty("cart_item_id") Long cartItemId,
    @JsonProperty("cart_id") Long cartId,
    @JsonProperty("product_variant_id") Long productVariantId,
    int quantity,
    @JsonProperty("price_at_time") BigDecimal priceAtTime,
    @JsonProperty("line_total") BigDecimal lineTotal,
    String message,
    @JsonProperty("cart_updated") CartUpdatedSummary cartUpdated) {}
