package org.sirantar.recadero.templates.service.dto;

import java.util.Map;

/**
 * Request payload for PUT /api/v1/templates/{id}. {@code meta} carries a
 * partial set of SEO fields (snake_case keys) merged into the template's
 * existing metadata, per admin's UpdateTemplateInput.
 */
public record UpdateTemplateRequest(String name, Map<String, Object> meta) {}
