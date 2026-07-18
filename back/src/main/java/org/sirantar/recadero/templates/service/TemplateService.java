package org.sirantar.recadero.templates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.templates.domain.Template;
import org.sirantar.recadero.templates.domain.TemplateStatus;
import org.sirantar.recadero.templates.domain.TemplateType;
import org.sirantar.recadero.templates.domain.TemplateVersion;
import org.sirantar.recadero.templates.events.TemplateEventPublisher;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.repository.TemplateRepository;
import org.sirantar.recadero.templates.repository.TemplateVersionRepository;
import org.sirantar.recadero.templates.service.dto.CreateTemplateRequest;
import org.sirantar.recadero.templates.service.dto.PublishResponse;
import org.sirantar.recadero.templates.service.dto.RevertResponse;
import org.sirantar.recadero.templates.service.dto.TemplateBlockResponse;
import org.sirantar.recadero.templates.service.dto.TemplateDetail;
import org.sirantar.recadero.templates.service.dto.TemplateMetaPayload;
import org.sirantar.recadero.templates.service.dto.TemplateSummary;
import org.sirantar.recadero.templates.service.dto.UnpublishResponse;
import org.sirantar.recadero.templates.service.dto.UpdateTemplateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Template CRUD and publishing lifecycle (DRAFT/PUBLISHED/ARCHIVED, versioning).
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final TemplateRepository templateRepository;
  private final TemplateBlockRepository templateBlockRepository;
  private final TemplateVersionRepository templateVersionRepository;
  private final TemplateBlockService templateBlockService;
  private final TemplateMetaService templateMetaService;
  private final TemplateEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public TemplateDetail createTemplate(CreateTemplateRequest request, String createdBy) {
    if (templateRepository.existsBySlug(request.slug())) {
      throw new ResourceConflictException(
          "DUPLICATE_SLUG",
          "Template slug must be unique",
          Map.of("field", "slug", "provided_slug", request.slug()));
    }

    Template template = new Template();
    template.setId(UUID.randomUUID());
    template.setName(request.name());
    template.setType(parseType(request.type()));
    template.setSlug(request.slug());
    template.setStatus(TemplateStatus.DRAFT);
    template.setVersion(1);
    template.setCreatedBy(createdBy);
    LocalDateTime now = LocalDateTime.now();
    template.setCreatedAt(now);
    template.setUpdatedAt(now);
    Template saved = templateRepository.save(template);

    templateMetaService.createEmpty(saved.getId());

    return toDetail(saved);
  }

  public PaginationResponse<TemplateSummary> listTemplates(
      TemplateType type, TemplateStatus status, String createdBy, Pageable pageable) {
    return PaginationResponse.from(
        templateRepository.search(type, status, createdBy, pageable).map(this::toSummary));
  }

  public TemplateDetail getTemplate(UUID id) {
    return toDetail(getTemplateOrThrow(id));
  }

  @Transactional
  public TemplateDetail updateTemplate(UUID id, UpdateTemplateRequest request) {
    Template template = getTemplateOrThrow(id);
    if (request.name() != null) {
      template.setName(request.name());
    }
    template.setUpdatedAt(LocalDateTime.now());
    Template saved = templateRepository.save(template);

    if (request.meta() != null) {
      templateMetaService.mergePartial(id, request.meta());
    }

    return toDetail(saved);
  }

  @Transactional
  public void archiveTemplate(UUID id) {
    Template template = getTemplateOrThrow(id);
    template.setStatus(TemplateStatus.ARCHIVED);
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.save(template);
    eventPublisher.publishArchived(template.getId().toString(), template.getName());
  }

  @Transactional
  public PublishResponse publishTemplate(UUID id, String changeNote, String publishedBy) {
    Template template = getTemplateOrThrow(id);

    TemplateVersion snapshot = new TemplateVersion();
    snapshot.setId(UUID.randomUUID());
    snapshot.setTemplateId(id);
    int versionNumber = templateVersionRepository.countByTemplateId(id) + 1;
    snapshot.setVersionNumber(versionNumber);
    snapshot.setContentSnapshotJson(writeSnapshot(id));
    LocalDateTime now = LocalDateTime.now();
    snapshot.setPublishedAt(now);
    snapshot.setCreatedBy(publishedBy);
    snapshot.setChangeNote(changeNote);
    templateVersionRepository.save(snapshot);

    template.setStatus(TemplateStatus.PUBLISHED);
    template.setVersion(versionNumber);
    template.setPublishedVersion(versionNumber);
    template.setPublishedAt(now);
    template.setUpdatedAt(now);
    templateRepository.save(template);

    eventPublisher.publishPublished(template.getId().toString(), template.getName(), versionNumber, template.getSlug());

    return new PublishResponse(
        template.getId().toString(),
        template.getStatus().name(),
        template.getVersion(),
        template.getPublishedVersion(),
        template.getPublishedAt(),
        "Template published successfully");
  }

  @Transactional
  public UnpublishResponse unpublishTemplate(UUID id) {
    Template template = getTemplateOrThrow(id);
    template.setStatus(TemplateStatus.DRAFT);
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.save(template);
    return new UnpublishResponse(
        template.getId().toString(), template.getStatus().name(), "Template unpublished. Previous version remains live.");
  }

  @Transactional
  public RevertResponse revertToVersion(UUID id, int versionNumber, String changeNote) {
    Template template = getTemplateOrThrow(id);
    TemplateVersion version = templateVersionRepository.findByTemplateIdAndVersionNumber(id, versionNumber)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Version " + versionNumber + " not found for template " + id));

    Snapshot snapshot = readSnapshot(version.getContentSnapshotJson());

    templateBlockRepository.deleteByTemplateId(id);
    List<TemplateBlockResponse> restored = snapshot.blocks().stream()
        .map(b -> templateBlockService.addBlock(id, toAddRequest(b)))
        .toList();

    template.setStatus(TemplateStatus.DRAFT);
    template.setVersion(template.getVersion() + 1);
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.save(template);

    return new RevertResponse(
        template.getId().toString(),
        template.getStatus().name(),
        template.getVersion(),
        "Template reverted to version " + versionNumber + ". New draft version " + template.getVersion()
            + " created for review.",
        restored);
  }

  public PaginationResponse<org.sirantar.recadero.templates.service.dto.TemplateVersionResponse> listVersions(
      UUID templateId, Pageable pageable) {
    return PaginationResponse.from(
        templateVersionRepository.findByTemplateIdOrderByVersionNumberDesc(templateId, pageable)
            .map(this::toVersionSummary));
  }

  public org.sirantar.recadero.templates.service.dto.TemplateVersionResponse getVersion(
      UUID templateId, int versionNumber) {
    TemplateVersion version = templateVersionRepository.findByTemplateIdAndVersionNumber(templateId, versionNumber)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Version " + versionNumber + " not found for template " + templateId));
    Snapshot snapshot = readSnapshot(version.getContentSnapshotJson());
    return new org.sirantar.recadero.templates.service.dto.TemplateVersionResponse(
        version.getId().toString(),
        version.getTemplateId().toString(),
        version.getVersionNumber(),
        version.getPublishedAt(),
        version.getCreatedBy(),
        version.getChangeNote(),
        snapshot.blocks(),
        snapshot.meta(),
        version.getPublishedAt());
  }

  private org.sirantar.recadero.templates.service.dto.TemplateVersionResponse toVersionSummary(TemplateVersion version) {
    return new org.sirantar.recadero.templates.service.dto.TemplateVersionResponse(
        version.getId().toString(),
        version.getTemplateId().toString(),
        version.getVersionNumber(),
        version.getPublishedAt(),
        version.getCreatedBy(),
        version.getChangeNote(),
        null,
        null,
        null);
  }

  /** Public GET /api/templates/{slug}: only ever returns a currently-published template. */
  public TemplateDetail getPublishedBySlug(String slug) {
    Template template = templateRepository.findBySlug(slug)
        .filter(t -> t.getStatus() == TemplateStatus.PUBLISHED)
        .orElseThrow(() -> new ResourceNotFoundException("No published template for slug: " + slug));
    return toDetail(template);
  }

  /** Admin preview: any status; a specific historical version if requested. */
  public TemplateDetail previewTemplate(UUID id, Integer versionNumber) {
    Template template = getTemplateOrThrow(id);
    if (versionNumber == null) {
      return toDetail(template);
    }

    TemplateVersion version = templateVersionRepository.findByTemplateIdAndVersionNumber(id, versionNumber)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Version " + versionNumber + " not found for template " + id));
    Snapshot snapshot = readSnapshot(version.getContentSnapshotJson());
    return new TemplateDetail(
        template.getId().toString(),
        template.getName(),
        template.getType().name(),
        template.getSlug(),
        template.getStatus().name(),
        versionNumber,
        template.getPublishedVersion(),
        snapshot.blocks(),
        snapshot.meta(),
        template.getCreatedBy(),
        template.getCreatedAt(),
        template.getPublishedAt());
  }

  private Template getTemplateOrThrow(UUID id) {
    return templateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
  }

  private TemplateType parseType(String value) {
    if (value == null) {
      throw new BusinessLogicException("INVALID_TEMPLATE_TYPE", "Unknown template type: null");
    }
    try {
      return TemplateType.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new BusinessLogicException("INVALID_TEMPLATE_TYPE", "Unknown template type: " + value);
    }
  }

  private TemplateSummary toSummary(Template template) {
    int blocksCount = templateBlockRepository.findByTemplateIdOrderByBlockOrder(template.getId()).size();
    return new TemplateSummary(
        template.getId().toString(),
        template.getName(),
        template.getType().name(),
        template.getSlug(),
        template.getStatus().name(),
        template.getVersion(),
        template.getPublishedVersion(),
        blocksCount,
        template.getCreatedBy(),
        template.getCreatedAt(),
        template.getPublishedAt());
  }

  private TemplateDetail toDetail(Template template) {
    List<TemplateBlockResponse> blocks = templateBlockService.listBlocks(template.getId());
    TemplateMetaPayload meta = templateMetaService.getPayload(template.getId());
    return new TemplateDetail(
        template.getId().toString(),
        template.getName(),
        template.getType().name(),
        template.getSlug(),
        template.getStatus().name(),
        template.getVersion(),
        template.getPublishedVersion(),
        blocks,
        meta,
        template.getCreatedBy(),
        template.getCreatedAt(),
        template.getPublishedAt());
  }

  private String writeSnapshot(UUID templateId) {
    try {
      Map<String, Object> snapshot = Map.of(
          "blocks", templateBlockService.listBlocks(templateId),
          "meta", templateMetaService.getPayload(templateId));
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to snapshot template " + templateId, e);
    }
  }

  private Snapshot readSnapshot(String json) {
    try {
      var node = objectMapper.readTree(json);
      List<TemplateBlockResponse> blocks = objectMapper.convertValue(
          node.get("blocks"), objectMapper.getTypeFactory().constructCollectionType(List.class, TemplateBlockResponse.class));
      TemplateMetaPayload meta = objectMapper.convertValue(node.get("meta"), TemplateMetaPayload.class);
      return new Snapshot(blocks, meta);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to read template snapshot", e);
    }
  }

  private org.sirantar.recadero.templates.service.dto.AddBlockRequest toAddRequest(TemplateBlockResponse block) {
    return new org.sirantar.recadero.templates.service.dto.AddBlockRequest(
        block.blockType(), block.blockName(), block.blockOrder(), block.content());
  }

  private record Snapshot(List<TemplateBlockResponse> blocks, TemplateMetaPayload meta) {}
}
