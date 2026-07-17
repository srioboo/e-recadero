package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload representing an entity-to-template mapping.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageContentMappingResponse(
    @JsonProperty("page_content_id") String pageContentId,
    @JsonProperty("template_id") String templateId,
    @JsonProperty("entity_id") String entityId,
    @JsonProperty("entity_type") String entityType,
    @JsonProperty("entity_name") String entityName,
    String status,
    @JsonProperty("published_at") LocalDateTime publishedAt) {}
