package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for PATCH /api/v1/templates/{id}/blocks/{blockId}/visibility.
 */
public record BlockVisibilityResponse(
    @JsonProperty("block_id") String blockId, @JsonProperty("is_visible") boolean isVisible) {}
