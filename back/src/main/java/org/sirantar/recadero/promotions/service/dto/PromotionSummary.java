package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/promotions.
 */
public record PromotionSummary(
    @JsonProperty("promotion_id") Long promotionId,
    String name,
    String type,
    @JsonProperty("discount_value") BigDecimal discountValue,
    String status,
    @JsonProperty("start_date") LocalDateTime startDate,
    @JsonProperty("end_date") LocalDateTime endDate,
    @JsonProperty("usage_limit") Integer usageLimit,
    @JsonProperty("current_usage") int currentUsage,
    int priority,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
