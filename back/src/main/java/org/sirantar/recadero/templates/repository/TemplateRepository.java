package org.sirantar.recadero.templates.repository;

import java.util.Optional;
import java.util.UUID;
import org.sirantar.recadero.templates.domain.Template;
import org.sirantar.recadero.templates.domain.TemplateStatus;
import org.sirantar.recadero.templates.domain.TemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for templates.
 */
public interface TemplateRepository extends JpaRepository<Template, UUID> {

  Optional<Template> findBySlug(String slug);

  boolean existsBySlug(String slug);

  @Query(
      """
      select t
      from Template t
      where (:type is null or t.type = :type)
        and (:status is null or t.status = :status)
        and (:createdBy is null or t.createdBy = :createdBy)
      order by t.createdAt desc
      """)
  Page<Template> search(
      @Param("type") TemplateType type,
      @Param("status") TemplateStatus status,
      @Param("createdBy") String createdBy,
      Pageable pageable);
}
