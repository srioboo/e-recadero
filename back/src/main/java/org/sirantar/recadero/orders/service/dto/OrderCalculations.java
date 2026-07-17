package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Order price breakdown.
 */
public record OrderCalculations(
    BigDecimal subtotal,
    @JsonProperty("tax_total") BigDecimal taxTotal,
    @JsonProperty("shipping_total") BigDecimal shippingTotal,
    @JsonProperty("discount_total") BigDecimal discountTotal,
    @JsonProperty("grand_total") BigDecimal grandTotal) {}
