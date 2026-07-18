package org.sirantar.recadero.templates.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.sirantar.recadero.templates.domain.TemplateStatus;
import org.sirantar.recadero.templates.domain.TemplateType;
import org.sirantar.recadero.templates.service.TemplateBlockService;
import org.sirantar.recadero.templates.service.TemplateMetaService;
import org.sirantar.recadero.templates.service.TemplateService;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.BlockVisibilityRequest;
import org.sirantar.recadero.templates.service.dto.BlockVisibilityResponse;
import org.sirantar.recadero.templates.service.dto.CreateTemplateRequest;
import org.sirantar.recadero.templates.service.dto.MessageResponse;
import org.sirantar.recadero.templates.service.dto.PublishRequest;
import org.sirantar.recadero.templates.service.dto.PublishResponse;
import org.sirantar.recadero.templates.service.dto.ReorderBlocksRequest;
import org.sirantar.recadero.templates.service.dto.TemplateBlockResponse;
import org.sirantar.recadero.templates.service.dto.TemplateDetail;
import org.sirantar.recadero.templates.service.dto.TemplateMetaPayload;
import org.sirantar.recadero.templates.service.dto.TemplateSummary;
import org.sirantar.recadero.templates.service.dto.TemplateVersionResponse;
import org.sirantar.recadero.templates.service.dto.UnpublishResponse;
import org.sirantar.recadero.templates.service.dto.UpdateMetaResponse;
import org.sirantar.recadero.templates.service.dto.UpdateTemplateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only template CRUD, block composition, metadata, and publish/unpublish.
 * See specs/002-backend-ecommerce/contracts/templates-contract.md.
 */
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@AdminOnly
public class TemplateAdminController {

  private final TemplateService templateService;
  private final TemplateBlockService templateBlockService;
  private final TemplateMetaService templateMetaService;

  @GetMapping
  public PaginationResponse<TemplateSummary> listTemplates(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status,
      @RequestParam(name = "created_by", required = false) String createdBy,
      Pageable pageable) {
    return templateService.listTemplates(
        type != null ? TemplateType.valueOf(type) : null,
        status != null ? TemplateStatus.valueOf(status) : null,
        createdBy,
        pageable);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TemplateDetail createTemplate(
      @RequestBody CreateTemplateRequest request, @AuthenticationPrincipal SecurityUser admin) {
    return templateService.createTemplate(request, admin.getUserId());
  }

  @GetMapping("/{id}")
  public TemplateDetail getTemplate(@PathVariable UUID id) {
    return templateService.getTemplate(id);
  }

  @PutMapping("/{id}")
  public TemplateDetail updateTemplate(@PathVariable UUID id, @RequestBody UpdateTemplateRequest request) {
    return templateService.updateTemplate(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void archiveTemplate(@PathVariable UUID id) {
    templateService.archiveTemplate(id);
  }

  @PostMapping("/{id}/blocks")
  @ResponseStatus(HttpStatus.CREATED)
  public TemplateBlockResponse addBlock(@PathVariable UUID id, @RequestBody AddBlockRequest request) {
    return templateBlockService.addBlock(id, request);
  }

  @PutMapping("/{id}/blocks/{blockId}")
  public TemplateBlockResponse updateBlock(
      @PathVariable UUID id,
      @PathVariable UUID blockId,
      @RequestBody org.sirantar.recadero.templates.service.dto.UpdateBlockRequest request) {
    return templateBlockService.updateBlock(id, blockId, request);
  }

  @PatchMapping("/{id}/blocks/{blockId}/visibility")
  public BlockVisibilityResponse setBlockVisibility(
      @PathVariable UUID id, @PathVariable UUID blockId, @RequestBody BlockVisibilityRequest request) {
    boolean isVisible = templateBlockService.setVisibility(id, blockId, request.isVisible());
    return new BlockVisibilityResponse(blockId.toString(), isVisible);
  }

  @DeleteMapping("/{id}/blocks/{blockId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBlock(@PathVariable UUID id, @PathVariable UUID blockId) {
    templateBlockService.deleteBlock(id, blockId);
  }

  @PostMapping("/{id}/blocks/reorder")
  public MessageResponse reorderBlocks(@PathVariable UUID id, @RequestBody ReorderBlocksRequest request) {
    templateBlockService.reorderBlocks(id, request);
    return new MessageResponse("Blocks reordered successfully");
  }

  @PutMapping("/{id}/meta")
  public UpdateMetaResponse updateMeta(@PathVariable UUID id, @RequestBody TemplateMetaPayload request) {
    return templateMetaService.updateMeta(id, request);
  }

  @PostMapping("/{id}/publish")
  public PublishResponse publish(
      @PathVariable UUID id, @RequestBody(required = false) PublishRequest request,
      @AuthenticationPrincipal SecurityUser admin) {
    String changeNote = request != null ? request.changeNote() : null;
    return templateService.publishTemplate(id, changeNote, admin.getUserId());
  }

  @PostMapping("/{id}/unpublish")
  public UnpublishResponse unpublish(@PathVariable UUID id) {
    return templateService.unpublishTemplate(id);
  }

  @GetMapping("/{id}/versions")
  public PaginationResponse<TemplateVersionResponse> listVersions(@PathVariable UUID id, Pageable pageable) {
    return templateService.listVersions(id, pageable);
  }
}
