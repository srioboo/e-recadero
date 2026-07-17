package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for GET /api/v1/cart.
 */
public record CartResponse(
    @JsonProperty("cart_id") Long cartId,
    @JsonProperty("user_id") Long userId,
    String status,
    List<CartItemDetail> items,
    @JsonProperty("applied_promotions") List<AppliedPromotion> appliedPromotions,
    CartCalculations calculations,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("expires_at") LocalDateTime expiresAt) {}
