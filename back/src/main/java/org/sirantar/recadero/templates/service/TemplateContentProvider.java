package org.sirantar.recadero.templates.service;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.templates.domain.EntityType;
import org.springframework.stereotype.Component;

/**
 * Best-effort display-name resolution for entities mapped to a template
 * (admin's "entities using this template" list is presentational only —
 * a missing/unparseable ID degrades to no name rather than an error).
 */
@Component
@RequiredArgsConstructor
public class TemplateContentProvider {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public String resolveEntityName(String entityId, EntityType entityType) {
    try {
      return switch (entityType) {
        case CATEGORY -> categoryRepository.findById(Long.valueOf(entityId))
            .map(org.sirantar.recadero.catalog.domain.Category::getName)
            .orElse(null);
        case PRODUCT -> productRepository.findById(Long.valueOf(entityId))
            .map(org.sirantar.recadero.catalog.domain.Product::getName)
            .orElse(null);
        case LANDING_PAGE, USER -> null;
      };
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
