package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Request payload for POST /api/v1/promotions/{id}/coupons.
 */
public record GenerateCouponsRequest(
    int count,
    @JsonProperty("code_prefix") String codePrefix,
    @JsonProperty("usage_limit") Integer usageLimit,
    @JsonProperty("expiry_date") LocalDateTime expiryDate) {}
