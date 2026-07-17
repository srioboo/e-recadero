package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Row shape for GET /api/v1/templates (list).
 */
public record TemplateSummary(
    @JsonProperty("template_id") String templateId,
    String name,
    String type,
    String slug,
    String status,
    int version,
    @JsonProperty("published_version") Integer publishedVersion,
    @JsonProperty("blocks_count") int blocksCount,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("published_at") LocalDateTime publishedAt) {}
