package org.sirantar.recadero.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.templates.domain.PageContentMapping;
import org.sirantar.recadero.templates.domain.Template;
import org.sirantar.recadero.templates.domain.TemplateBlock;
import org.sirantar.recadero.templates.domain.TemplateMeta;
import org.sirantar.recadero.templates.events.TemplateEventPublisher;
import org.sirantar.recadero.templates.repository.PageContentMappingRepository;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.repository.TemplateMetaRepository;
import org.sirantar.recadero.templates.repository.TemplateRepository;
import org.sirantar.recadero.templates.repository.TemplateVersionRepository;
import org.sirantar.recadero.templates.service.PageContentService;
import org.sirantar.recadero.templates.service.TemplateBlockService;
import org.sirantar.recadero.templates.service.TemplateContentProvider;
import org.sirantar.recadero.templates.service.TemplateMetaService;
import org.sirantar.recadero.templates.service.TemplateService;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.CreateTemplateRequest;
import org.sirantar.recadero.templates.service.dto.MapEntityRequest;
import org.sirantar.recadero.templates.service.dto.PageContentMappingResponse;
import org.sirantar.recadero.templates.service.dto.PublishResponse;
import org.sirantar.recadero.templates.service.dto.TemplateDetail;
import org.sirantar.recadero.templates.service.dto.TemplateMetaPayload;

/**
 * End-to-end workflow test for the Templates module: create → add blocks →
 * set meta → publish → map to a category, exercising the service layer with
 * mocked persistence (mirrors CatalogIntegrationTest/UserIntegrationTest).
 */
@DisplayName("Templates Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class TemplateIntegrationTest {

  @Mock private TemplateRepository templateRepository;
  @Mock private TemplateBlockRepository templateBlockRepository;
  @Mock private TemplateMetaRepository templateMetaRepository;
  @Mock private TemplateVersionRepository templateVersionRepository;
  @Mock private PageContentMappingRepository pageContentMappingRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private ProductRepository productRepository;
  @Mock private TemplateEventPublisher eventPublisher;

  private TemplateService templateService;
  private TemplateBlockService templateBlockService;
  private TemplateMetaService templateMetaService;
  private PageContentService pageContentService;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    templateBlockService = new TemplateBlockService(templateBlockRepository, objectMapper);
    templateMetaService = new TemplateMetaService(templateMetaRepository, objectMapper);
    templateService = new TemplateService(
        templateRepository,
        templateBlockRepository,
        templateVersionRepository,
        templateBlockService,
        templateMetaService,
        eventPublisher,
        objectMapper);
    TemplateContentProvider contentProvider = new TemplateContentProvider(categoryRepository, productRepository);
    pageContentService = new PageContentService(pageContentMappingRepository, contentProvider);
  }

  @Test
  @DisplayName("Should create template, add a block, set meta, publish, and map to a category")
  void createAddBlocksAddMetaPublishMapToCategoryFlow() {
    when(templateRepository.existsBySlug("summer-sale")).thenReturn(false);
    when(templateRepository.save(any(Template.class))).thenAnswer(inv -> inv.getArgument(0));
    when(templateMetaRepository.save(any(TemplateMeta.class))).thenAnswer(inv -> inv.getArgument(0));

    TemplateDetail created = templateService.createTemplate(
        new CreateTemplateRequest("Summer Sale", "LANDING_PAGE", "summer-sale"), "admin-1");

    assertThat(created.status()).isEqualTo("DRAFT");
    assertThat(created.slug()).isEqualTo("summer-sale");
    UUID templateId = UUID.fromString(created.templateId());

    // Add a HERO block
    when(templateBlockRepository.save(any(TemplateBlock.class))).thenAnswer(inv -> inv.getArgument(0));
    var block = templateBlockService.addBlock(
        templateId, new AddBlockRequest("HERO", "Main hero", 1, Map.of("title", "Summer Sale 2026")));
    assertThat(block.blockType()).isEqualTo("HERO");

    when(templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId)).thenReturn(List.of(blockEntity(templateId)));

    // Set meta
    when(templateMetaRepository.findByTemplateId(templateId)).thenReturn(Optional.of(metaEntity(templateId)));
    var metaResponse = templateMetaService.updateMeta(
        templateId,
        new TemplateMetaPayload("Summer Sale 2026", "Up to 70% off", null, null, null, null, null, null, null));
    assertThat(metaResponse.meta().pageTitle()).isEqualTo("Summer Sale 2026");

    // Publish
    Template template = templateEntity(templateId);
    when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    when(templateVersionRepository.countByTemplateId(templateId)).thenReturn(0);

    PublishResponse published = templateService.publishTemplate(templateId, "Initial launch", "admin-1");
    assertThat(published.status()).isEqualTo("PUBLISHED");
    assertThat(published.version()).isEqualTo(1);

    // Map to a category
    Category electronics = new Category();
    electronics.setId(5L);
    electronics.setName("Electronics");
    when(categoryRepository.findById(5L)).thenReturn(Optional.of(electronics));
    when(pageContentMappingRepository.findByEntityIdAndEntityType("5", org.sirantar.recadero.templates.domain.EntityType.CATEGORY))
        .thenReturn(Optional.empty());
    when(pageContentMappingRepository.save(any(PageContentMapping.class))).thenAnswer(inv -> inv.getArgument(0));

    PageContentMappingResponse mapping =
        pageContentService.mapTemplateToEntity(templateId, new MapEntityRequest("5", "CATEGORY", "PUBLISHED"));

    assertThat(mapping.entityName()).isEqualTo("Electronics");
    assertThat(mapping.status()).isEqualTo("PUBLISHED");
  }

  private TemplateBlock blockEntity(UUID templateId) {
    TemplateBlock block = new TemplateBlock();
    block.setId(UUID.randomUUID());
    block.setTemplateId(templateId);
    block.setBlockType(org.sirantar.recadero.templates.domain.BlockType.HERO);
    block.setBlockName("Main hero");
    block.setBlockOrder(1);
    block.setIsVisible(true);
    block.setContentJson("{\"title\":\"Summer Sale 2026\"}");
    block.setCreatedAt(LocalDateTime.now());
    block.setUpdatedAt(LocalDateTime.now());
    return block;
  }

  private TemplateMeta metaEntity(UUID templateId) {
    TemplateMeta meta = new TemplateMeta();
    meta.setId(UUID.randomUUID());
    meta.setTemplateId(templateId);
    meta.setUpdatedAt(LocalDateTime.now());
    return meta;
  }

  private Template templateEntity(UUID templateId) {
    Template template = new Template();
    template.setId(templateId);
    template.setName("Summer Sale");
    template.setType(org.sirantar.recadero.templates.domain.TemplateType.LANDING_PAGE);
    template.setSlug("summer-sale");
    template.setStatus(org.sirantar.recadero.templates.domain.TemplateStatus.DRAFT);
    template.setVersion(1);
    template.setCreatedAt(LocalDateTime.now());
    template.setUpdatedAt(LocalDateTime.now());
    return template;
  }
}
