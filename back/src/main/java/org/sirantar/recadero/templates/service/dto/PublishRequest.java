package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST .../publish and POST .../revert/{versionNumber}.
 */
public record PublishRequest(@JsonProperty("change_note") String changeNote) {}
