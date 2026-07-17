package org.sirantar.recadero.templates.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for the Templates module's domain events.
 */
@Component
@RequiredArgsConstructor
public class TemplateEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishPublished(String templateId, String templateName, int version, String slug) {
    eventPublisher.publishEvent(new TemplatePublishedEvent(templateId, templateName, version, slug));
  }

  public void publishArchived(String templateId, String templateName) {
    eventPublisher.publishEvent(new TemplateArchivedEvent(templateId, templateName));
  }
}
