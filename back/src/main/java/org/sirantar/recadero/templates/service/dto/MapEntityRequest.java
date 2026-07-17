package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/templates/{id}/map-entity.
 */
public record MapEntityRequest(
    @JsonProperty("entity_id") String entityId,
    @JsonProperty("entity_type") String entityType,
    String status) {}
