package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for PATCH /api/v1/templates/{id}/blocks/{blockId}/visibility.
 */
public record BlockVisibilityRequest(@JsonProperty("is_visible") boolean isVisible) {}
