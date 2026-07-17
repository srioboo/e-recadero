package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/cart/prepare-checkout.
 */
public record PrepareCheckoutRequest(
    @JsonProperty("shipping_method_id") String shippingMethodId,
    @JsonProperty("billing_address_id") Long billingAddressId,
    @JsonProperty("shipping_address_id") Long shippingAddressId) {}
