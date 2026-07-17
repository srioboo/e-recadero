package org.sirantar.recadero.templates.events;

/**
 * Published when a template is archived.
 */
public record TemplateArchivedEvent(String templateId, String templateName) {}
