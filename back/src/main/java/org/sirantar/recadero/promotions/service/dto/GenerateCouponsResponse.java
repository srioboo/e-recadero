package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response payload for POST /api/v1/promotions/{id}/coupons.
 */
public record GenerateCouponsResponse(
    @JsonProperty("generated_count") int generatedCount,
    @JsonProperty("coupon_codes") List<String> couponCodes,
    @JsonProperty("download_link") String downloadLink) {}
