package org.sirantar.recadero.templates.repository;

import java.util.List;
import java.util.UUID;
import org.sirantar.recadero.templates.domain.TemplateBlock;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for template content blocks.
 */
public interface TemplateBlockRepository extends JpaRepository<TemplateBlock, UUID> {

  List<TemplateBlock> findByTemplateIdOrderByBlockOrder(UUID templateId);

  void deleteByTemplateId(UUID templateId);
}
