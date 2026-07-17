package org.sirantar.recadero.templates.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.sirantar.recadero.templates.domain.EntityType;
import org.sirantar.recadero.templates.domain.MappingStatus;
import org.sirantar.recadero.templates.domain.PageContentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for template-to-entity page content mappings.
 */
public interface PageContentMappingRepository extends JpaRepository<PageContentMapping, UUID> {

  Optional<PageContentMapping> findByEntityIdAndEntityType(String entityId, EntityType entityType);

  @Query(
      """
      select m
      from PageContentMapping m
      where m.templateId = :templateId
        and (:entityType is null or m.entityType = :entityType)
        and (:status is null or m.status = :status)
      order by m.createdAt desc
      """)
  List<PageContentMapping> findForTemplate(
      @Param("templateId") UUID templateId,
      @Param("entityType") EntityType entityType,
      @Param("status") MappingStatus status);

  void deleteByTemplateIdAndEntityId(UUID templateId, String entityId);
}
