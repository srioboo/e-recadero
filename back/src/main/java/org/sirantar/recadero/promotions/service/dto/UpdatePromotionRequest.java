package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request payload for PUT /api/v1/promotions/{id}. All fields optional (partial update).
 */
public record UpdatePromotionRequest(
    String name,
    @JsonProperty("discount_value") BigDecimal discountValue,
    @JsonProperty("max_discount_amount") BigDecimal maxDiscountAmount,
    @JsonProperty("minimum_order_amount") BigDecimal minimumOrderAmount,
    @JsonProperty("start_date") LocalDateTime startDate,
    @JsonProperty("end_date") LocalDateTime endDate,
    @JsonProperty("usage_limit") Integer usageLimit,
    Integer priority) {}
