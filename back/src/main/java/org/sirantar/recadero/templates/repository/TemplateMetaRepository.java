package org.sirantar.recadero.templates.repository;

import java.util.Optional;
import java.util.UUID;
import org.sirantar.recadero.templates.domain.TemplateMeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for template SEO metadata.
 */
public interface TemplateMetaRepository extends JpaRepository<TemplateMeta, UUID> {

  Optional<TemplateMeta> findByTemplateId(UUID templateId);
}
