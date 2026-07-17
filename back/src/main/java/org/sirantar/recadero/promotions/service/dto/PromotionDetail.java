package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for POST/GET/PUT /api/v1/promotions/{id}.
 */
public record PromotionDetail(
    @JsonProperty("promotion_id") Long promotionId,
    String name,
    @JsonProperty("promotion_type") String promotionType,
    @JsonProperty("discount_value") BigDecimal discountValue,
    @JsonProperty("max_discount_amount") BigDecimal maxDiscountAmount,
    @JsonProperty("minimum_order_amount") BigDecimal minimumOrderAmount,
    String status,
    @JsonProperty("start_date") LocalDateTime startDate,
    @JsonProperty("end_date") LocalDateTime endDate,
    @JsonProperty("usage_limit") Integer usageLimit,
    @JsonProperty("current_usage") int currentUsage,
    int priority,
    List<PromotionRuleResponse> rules,
    List<CouponSummary> coupons,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
