package org.sirantar.recadero.templates.repository;

import java.util.Optional;
import java.util.UUID;
import org.sirantar.recadero.templates.domain.TemplateVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for immutable template version snapshots.
 */
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, UUID> {

  Optional<TemplateVersion> findByTemplateIdAndVersionNumber(UUID templateId, Integer versionNumber);

  Page<TemplateVersion> findByTemplateIdOrderByVersionNumberDesc(UUID templateId, Pageable pageable);

  int countByTemplateId(UUID templateId);
}
