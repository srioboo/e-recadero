package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for POST /api/v1/templates/{id}/unpublish.
 */
public record UnpublishResponse(
    @JsonProperty("template_id") String templateId, String status, String message) {}
