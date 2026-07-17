package org.sirantar.recadero.templates.api;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.sirantar.recadero.templates.domain.EntityType;
import org.sirantar.recadero.templates.domain.MappingStatus;
import org.sirantar.recadero.templates.service.PageContentService;
import org.sirantar.recadero.templates.service.TemplateService;
import org.sirantar.recadero.templates.service.dto.MapEntityRequest;
import org.sirantar.recadero.templates.service.dto.PageContentMappingResponse;
import org.sirantar.recadero.templates.service.dto.PublishRequest;
import org.sirantar.recadero.templates.service.dto.RevertResponse;
import org.sirantar.recadero.templates.service.dto.TemplateVersionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only version snapshot retrieval, revert, and entity-mapping management.
 * (Entity mapping endpoints are documented in templates-contract.md but were
 * undercounted in tasks.md's T170/T171 endpoint tallies; admin's api.ts calls
 * them, so they're implemented here alongside the closely related T171 work.)
 */
@RestController
@RequestMapping("/api/v1/templates/{id}")
@RequiredArgsConstructor
@AdminOnly
public class TemplateVersionController {

  private final TemplateService templateService;
  private final PageContentService pageContentService;

  @GetMapping("/versions/{versionNumber}")
  public TemplateVersionResponse getVersion(@PathVariable UUID id, @PathVariable int versionNumber) {
    return templateService.getVersion(id, versionNumber);
  }

  @PostMapping("/revert/{versionNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  public RevertResponse revert(
      @PathVariable UUID id, @PathVariable int versionNumber, @RequestBody(required = false) PublishRequest request) {
    String changeNote = request != null ? request.changeNote() : null;
    return templateService.revertToVersion(id, versionNumber, changeNote);
  }

  @GetMapping("/entities")
  public List<PageContentMappingResponse> getEntities(
      @PathVariable UUID id,
      @RequestParam(name = "entity_type", required = false) String entityType,
      @RequestParam(required = false) String status) {
    return pageContentService.getEntitiesForTemplate(
        id,
        entityType != null ? EntityType.valueOf(entityType) : null,
        status != null ? MappingStatus.valueOf(status) : null);
  }

  @PostMapping("/map-entity")
  @ResponseStatus(HttpStatus.CREATED)
  public PageContentMappingResponse mapEntity(@PathVariable UUID id, @RequestBody MapEntityRequest request) {
    return pageContentService.mapTemplateToEntity(id, request);
  }

  @DeleteMapping("/map-entity/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unmapEntity(@PathVariable UUID id, @PathVariable String entityId) {
    pageContentService.unmapEntity(id, entityId);
  }
}
