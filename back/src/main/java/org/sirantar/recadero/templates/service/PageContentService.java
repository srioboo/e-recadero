package org.sirantar.recadero.templates.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.templates.domain.EntityType;
import org.sirantar.recadero.templates.domain.MappingStatus;
import org.sirantar.recadero.templates.domain.PageContentMapping;
import org.sirantar.recadero.templates.repository.PageContentMappingRepository;
import org.sirantar.recadero.templates.service.dto.MapEntityRequest;
import org.sirantar.recadero.templates.service.dto.PageContentMappingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps templates to the products/categories/landing pages/users that render them.
 */
@Service
@RequiredArgsConstructor
public class PageContentService {

  private final PageContentMappingRepository pageContentMappingRepository;
  private final TemplateContentProvider templateContentProvider;

  @Transactional
  public PageContentMappingResponse mapTemplateToEntity(UUID templateId, MapEntityRequest request) {
    EntityType entityType = EntityType.valueOf(request.entityType());

    // Reassignment replaces rather than duplicates: at most one active
    // mapping per entity, per the contract's map-entity semantics.
    PageContentMapping mapping = pageContentMappingRepository
        .findByEntityIdAndEntityType(request.entityId(), entityType)
        .orElseGet(PageContentMapping::new);

    boolean isNew = mapping.getId() == null;
    if (isNew) {
      mapping.setId(UUID.randomUUID());
      mapping.setEntityId(request.entityId());
      mapping.setEntityType(entityType);
      mapping.setCreatedAt(LocalDateTime.now());
    }
    mapping.setTemplateId(templateId);
    MappingStatus status = request.status() != null ? MappingStatus.valueOf(request.status()) : MappingStatus.PUBLISHED;
    mapping.setStatus(status);
    if (status == MappingStatus.PUBLISHED) {
      mapping.setPublishedAt(LocalDateTime.now());
    }

    return toResponse(pageContentMappingRepository.save(mapping));
  }

  @Transactional
  public void unmapEntity(UUID templateId, String entityId) {
    pageContentMappingRepository.deleteByTemplateIdAndEntityId(templateId, entityId);
  }

  public List<PageContentMappingResponse> getEntitiesForTemplate(
      UUID templateId, EntityType entityType, MappingStatus status) {
    return pageContentMappingRepository.findForTemplate(templateId, entityType, status).stream()
        .map(this::toResponse)
        .toList();
  }

  private PageContentMappingResponse toResponse(PageContentMapping mapping) {
    return new PageContentMappingResponse(
        mapping.getId().toString(),
        mapping.getTemplateId().toString(),
        mapping.getEntityId(),
        mapping.getEntityType().name(),
        templateContentProvider.resolveEntityName(mapping.getEntityId(), mapping.getEntityType()),
        mapping.getStatus().name(),
        mapping.getPublishedAt());
  }
}
