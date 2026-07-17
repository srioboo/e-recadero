package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for POST /api/v1/templates/{id}/publish.
 */
public record PublishResponse(
    @JsonProperty("template_id") String templateId,
    String status,
    int version,
    @JsonProperty("published_version") int publishedVersion,
    @JsonProperty("published_at") LocalDateTime publishedAt,
    String message) {}
