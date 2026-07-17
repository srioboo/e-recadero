package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/cart/confirm-checkout.
 */
public record ConfirmCheckoutRequest(
    @JsonProperty("checkout_token") String checkoutToken,
    @JsonProperty("payment_method") String paymentMethod,
    @JsonProperty("transaction_id") String transactionId) {}
