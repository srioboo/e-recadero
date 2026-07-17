package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for DELETE /api/v1/cart/remove-coupon.
 */
public record RemoveCouponResponse(String message, @JsonProperty("cart_updated") CartUpdatedSummary cartUpdated) {}
