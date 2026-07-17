package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for PUT /api/v1/promotions/{id}/coupons/{couponId}.
 */
public record UpdateCouponRequest(@JsonProperty("is_active") Boolean isActive, @JsonProperty("usage_limit") Integer usageLimit) {}
