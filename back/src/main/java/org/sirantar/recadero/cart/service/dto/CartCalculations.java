package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Cart price breakdown, per calculateTotals().
 */
public record CartCalculations(
    BigDecimal subtotal,
    @JsonProperty("discount_total") BigDecimal discountTotal,
    @JsonProperty("tax_total") BigDecimal taxTotal,
    @JsonProperty("shipping_total") BigDecimal shippingTotal,
    @JsonProperty("grand_total") BigDecimal grandTotal) {}
