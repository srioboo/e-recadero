package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * A purchased line item as shown in order responses.
 */
public record OrderItemDetail(
    @JsonProperty("order_item_id") Long orderItemId,
    @JsonProperty("product_name") String productName,
    @JsonProperty("product_sku") String productSku,
    int quantity,
    @JsonProperty("unit_price") BigDecimal unitPrice,
    @JsonProperty("line_discount") BigDecimal lineDiscount,
    @JsonProperty("line_total") BigDecimal lineTotal) {}
