package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for POST /api/v1/promotions.
 */
public record CreatePromotionResponse(
    @JsonProperty("promotion_id") Long promotionId,
    String name,
    String status,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
