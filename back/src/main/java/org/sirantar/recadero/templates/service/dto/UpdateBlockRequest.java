package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Request payload for PUT /api/v1/templates/{id}/blocks/{blockId}.
 */
public record UpdateBlockRequest(
    Map<String, Object> content, @JsonProperty("block_order") Integer blockOrder) {}
