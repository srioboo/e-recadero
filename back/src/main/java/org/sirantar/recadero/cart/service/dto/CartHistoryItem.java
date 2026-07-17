package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/cart/history.
 */
public record CartHistoryItem(
    @JsonProperty("cart_id") Long cartId,
    @JsonProperty("items_count") int itemsCount,
    @JsonProperty("grand_total") BigDecimal grandTotal,
    String status,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("expires_at") LocalDateTime expiresAt) {}
