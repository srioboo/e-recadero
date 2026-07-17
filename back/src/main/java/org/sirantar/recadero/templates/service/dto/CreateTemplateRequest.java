package org.sirantar.recadero.templates.service.dto;

/**
 * Request payload for POST /api/v1/templates.
 */
public record CreateTemplateRequest(String name, String type, String slug) {}
