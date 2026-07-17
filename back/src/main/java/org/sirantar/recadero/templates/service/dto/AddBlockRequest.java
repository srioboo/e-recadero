package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Request payload for POST /api/v1/templates/{id}/blocks.
 */
public record AddBlockRequest(
    @JsonProperty("block_type") String blockType,
    @JsonProperty("block_name") String blockName,
    @JsonProperty("block_order") Integer blockOrder,
    Map<String, Object> content) {}
