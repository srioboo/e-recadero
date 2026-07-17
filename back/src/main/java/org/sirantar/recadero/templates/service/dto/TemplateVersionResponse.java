package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Version history row (GET .../versions) and single-version snapshot
 * (GET .../versions/{n}) — the latter additionally populates blocks/meta.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TemplateVersionResponse(
    @JsonProperty("version_id") String versionId,
    @JsonProperty("template_id") String templateId,
    @JsonProperty("version_number") int versionNumber,
    @JsonProperty("published_at") LocalDateTime publishedAt,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("change_note") String changeNote,
    List<TemplateBlockResponse> blocks,
    TemplateMetaPayload meta,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
