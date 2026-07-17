package org.sirantar.recadero.templates.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.templates.domain.Template;
import org.sirantar.recadero.templates.domain.TemplateStatus;
import org.sirantar.recadero.templates.domain.TemplateType;
import org.sirantar.recadero.templates.domain.TemplateVersion;
import org.sirantar.recadero.templates.events.TemplateEventPublisher;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.repository.TemplateRepository;
import org.sirantar.recadero.templates.repository.TemplateVersionRepository;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.RevertResponse;
import org.sirantar.recadero.templates.service.dto.TemplateBlockResponse;
import org.sirantar.recadero.templates.service.dto.TemplateMetaPayload;

/** Covers TemplateService's publish snapshot creation and revert-to-draft logic. */
@ExtendWith(MockitoExtension.class)
class TemplateVersionServiceTest {

  @Mock private TemplateRepository templateRepository;
  @Mock private TemplateBlockRepository templateBlockRepository;
  @Mock private TemplateVersionRepository templateVersionRepository;
  @Mock private TemplateBlockService templateBlockService;
  @Mock private TemplateMetaService templateMetaService;
  @Mock private TemplateEventPublisher eventPublisher;

  private TemplateService templateService;
  private UUID templateId;
  private Template template;

  @BeforeEach
  void setUp() {
    templateService = new TemplateService(
        templateRepository,
        templateBlockRepository,
        templateVersionRepository,
        templateBlockService,
        templateMetaService,
        eventPublisher,
        new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));

    templateId = UUID.randomUUID();
    template = new Template();
    template.setId(templateId);
    template.setName("Summer Sale");
    template.setType(TemplateType.LANDING_PAGE);
    template.setSlug("summer-sale");
    template.setStatus(TemplateStatus.DRAFT);
    template.setVersion(1);
    template.setCreatedAt(LocalDateTime.now());
    template.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void publishTemplateCreatesSnapshotAndIncrementsPublishedVersion() {
    when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    when(templateVersionRepository.countByTemplateId(templateId)).thenReturn(0);
    when(templateBlockService.listBlocks(templateId)).thenReturn(List.of());
    when(templateMetaService.getPayload(templateId)).thenReturn(emptyMeta());
    when(templateRepository.save(any(Template.class))).thenAnswer(inv -> inv.getArgument(0));

    var response = templateService.publishTemplate(templateId, "Initial launch", "admin-1");

    assertThat(response.status()).isEqualTo("PUBLISHED");
    assertThat(response.version()).isEqualTo(1);
    assertThat(response.publishedVersion()).isEqualTo(1);
    verify(templateVersionRepository).save(any(TemplateVersion.class));
    verify(eventPublisher).publishPublished(templateId.toString(), "Summer Sale", 1, "summer-sale");
  }

  @Test
  void revertToVersionRecreatesBlocksFromSnapshotAndBumpsDraftVersion() {
    template.setVersion(2);
    template.setStatus(TemplateStatus.PUBLISHED);

    TemplateBlockResponse snapshotBlock =
        new TemplateBlockResponse(UUID.randomUUID().toString(), templateId.toString(), "HERO", "Hero", 1, true,
            java.util.Map.of("title", "Old headline"), LocalDateTime.now());

    TemplateVersion version = new TemplateVersion();
    version.setId(UUID.randomUUID());
    version.setTemplateId(templateId);
    version.setVersionNumber(1);
    version.setPublishedAt(LocalDateTime.now());
    version.setContentSnapshotJson(
        "{\"blocks\":[{\"block_id\":\"" + snapshotBlock.blockId() + "\",\"template_id\":\"" + templateId
            + "\",\"block_type\":\"HERO\",\"block_name\":\"Hero\",\"block_order\":1,\"is_visible\":true,"
            + "\"content\":{\"title\":\"Old headline\"}}],\"meta\":{}}");

    when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    when(templateVersionRepository.findByTemplateIdAndVersionNumber(templateId, 1)).thenReturn(Optional.of(version));
    when(templateBlockService.addBlock(any(UUID.class), any(AddBlockRequest.class))).thenReturn(snapshotBlock);
    when(templateRepository.save(any(Template.class))).thenAnswer(inv -> inv.getArgument(0));

    RevertResponse response = templateService.revertToVersion(templateId, 1, "Old design performed better");

    assertThat(response.status()).isEqualTo("DRAFT");
    assertThat(response.version()).isEqualTo(3);
    assertThat(response.blocks()).hasSize(1);
    verify(templateBlockRepository).deleteByTemplateId(templateId);
  }

  private TemplateMetaPayload emptyMeta() {
    return new TemplateMetaPayload(null, null, null, null, null, null, null, null, null);
  }
}
