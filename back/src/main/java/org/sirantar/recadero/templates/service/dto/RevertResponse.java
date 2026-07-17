package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response payload for POST /api/v1/templates/{id}/revert/{versionNumber}.
 */
public record RevertResponse(
    @JsonProperty("template_id") String templateId,
    String status,
    int version,
    String message,
    List<TemplateBlockResponse> blocks) {}
