package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

/**
 * A cart line item enriched with catalog display data, for GET /api/v1/cart.
 */
public record CartItemDetail(
    @JsonProperty("cart_item_id") Long cartItemId,
    @JsonProperty("product_variant_id") Long productVariantId,
    @JsonProperty("product_name") String productName,
    @JsonProperty("product_sku") String productSku,
    @JsonProperty("variant_attributes") Map<String, Object> variantAttributes,
    int quantity,
    @JsonProperty("price_at_time") BigDecimal priceAtTime,
    @JsonProperty("discount_applied") BigDecimal discountApplied,
    @JsonProperty("line_total") BigDecimal lineTotal,
    @JsonProperty("is_in_stock") boolean isInStock) {}
