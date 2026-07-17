package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for PUT /api/v1/templates/{id}/meta.
 */
public record UpdateMetaResponse(
    @JsonProperty("template_id") String templateId,
    TemplateMetaPayload meta,
    @JsonProperty("updated_at") LocalDateTime updatedAt) {}
