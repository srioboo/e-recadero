package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full template shape: create/get/update/publish-preview responses, and the
 * public GET /api/templates/{slug} endpoint.
 */
public record TemplateDetail(
    @JsonProperty("template_id") String templateId,
    String name,
    String type,
    String slug,
    String status,
    int version,
    @JsonProperty("published_version") Integer publishedVersion,
    List<TemplateBlockResponse> blocks,
    TemplateMetaPayload meta,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("published_at") LocalDateTime publishedAt) {}
