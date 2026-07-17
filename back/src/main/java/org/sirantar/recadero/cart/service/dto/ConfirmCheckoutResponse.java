package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload for POST /api/v1/cart/confirm-checkout. {@code order_id}/
 * {@code order_number}/{@code next_step} are null until the Orders module
 * (Phase 6, not yet implemented) exists to consume {@code CheckoutCompletedEvent}
 * and create the actual order — see CheckoutService.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfirmCheckoutResponse(
    @JsonProperty("order_id") String orderId,
    @JsonProperty("order_number") String orderNumber,
    @JsonProperty("cart_id") Long cartId,
    @JsonProperty("grand_total") BigDecimal grandTotal,
    String message,
    @JsonProperty("next_step") String nextStep) {}
