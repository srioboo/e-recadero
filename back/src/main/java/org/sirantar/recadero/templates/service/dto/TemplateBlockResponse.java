package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response payload representing a single template content block.
 */
public record TemplateBlockResponse(
    @JsonProperty("block_id") String blockId,
    @JsonProperty("template_id") String templateId,
    @JsonProperty("block_type") String blockType,
    @JsonProperty("block_name") String blockName,
    @JsonProperty("block_order") int blockOrder,
    @JsonProperty("is_visible") boolean isVisible,
    Map<String, Object> content,
    @JsonProperty("created_at") LocalDateTime createdAt) {}
