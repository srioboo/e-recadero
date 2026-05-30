package org.sirantar.recadero.catalog.service.dto;

import java.time.Instant;
import java.util.List;

/**
 * Product availability view model.
 */
public record ProductAvailabilityResponse(
    Long productId, List<ProductVariantAvailabilityResponse> variants, Instant timestamp) {}
