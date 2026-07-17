package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for POST /api/v1/cart/prepare-checkout.
 */
public record PrepareCheckoutResponse(
    @JsonProperty("cart_id") Long cartId,
    String status,
    @JsonProperty("checkout_token") String checkoutToken,
    CartCalculations calculations,
    @JsonProperty("expires_at") LocalDateTime expiresAt,
    String message) {}
