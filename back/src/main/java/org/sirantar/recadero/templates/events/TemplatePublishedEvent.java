package org.sirantar.recadero.templates.events;

/**
 * Published when a template is published.
 */
public record TemplatePublishedEvent(String templateId, String templateName, int version, String slug) {}
