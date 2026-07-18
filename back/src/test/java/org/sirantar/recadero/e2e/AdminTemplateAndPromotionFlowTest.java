package org.sirantar.recadero.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.sirantar.recadero.promotions.service.PromotionService;
import org.sirantar.recadero.promotions.service.PromotionValidationService;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionRequest;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionResponse;
import org.sirantar.recadero.templates.domain.Template;
import org.sirantar.recadero.templates.domain.TemplateBlock;
import org.sirantar.recadero.templates.domain.TemplateMeta;
import org.sirantar.recadero.templates.events.TemplateEventPublisher;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.repository.TemplateMetaRepository;
import org.sirantar.recadero.templates.repository.TemplateRepository;
import org.sirantar.recadero.templates.repository.TemplateVersionRepository;
import org.sirantar.recadero.templates.service.TemplateBlockService;
import org.sirantar.recadero.templates.service.TemplateMetaService;
import org.sirantar.recadero.templates.service.TemplateService;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.CreateTemplateRequest;
import org.sirantar.recadero.templates.service.dto.TemplateDetail;

/**
 * Admin authoring journey (T182): a landing page template advertising a
 * promotion is built and published, while the promotion itself is created
 * and goes live — verifying both are independently queryable once
 * published. There is no CDN/HTTP cache layer in this codebase (see
 * templates-contract.md's "cache invalidated" step), so that part of the
 * scenario doesn't apply; publishing a template is itself the only
 * "become visible" transition that exists here.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("E2E: admin creates a landing page template and an accompanying promotion")
class AdminTemplateAndPromotionFlowTest {

  @Mock private TemplateRepository templateRepository;
  @Mock private TemplateBlockRepository templateBlockRepository;
  @Mock private TemplateMetaRepository templateMetaRepository;
  @Mock private TemplateVersionRepository templateVersionRepository;
  @Mock private TemplateEventPublisher templateEventPublisher;

  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionRuleRepository promotionRuleRepository;
  @Mock private CouponCodeRepository couponCodeRepository;
  @Mock private PromotionEventPublisher promotionEventPublisher;

  private TemplateService templateService;
  private TemplateBlockService templateBlockService;
  private PromotionService promotionService;

  private Template savedTemplate;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    templateBlockService = new TemplateBlockService(templateBlockRepository, objectMapper);
    TemplateMetaService templateMetaService = new TemplateMetaService(templateMetaRepository, objectMapper);
    templateService = new TemplateService(
        templateRepository, templateBlockRepository, templateVersionRepository,
        templateBlockService, templateMetaService, templateEventPublisher, objectMapper);
    promotionService = new PromotionService(
        promotionRepository, promotionRuleRepository, couponCodeRepository,
        new PromotionValidationService(), promotionEventPublisher, objectMapper);

    when(templateRepository.save(any(Template.class))).thenAnswer(inv -> {
      // Unlike the other modules' Long/IDENTITY entities, Template assigns
      // its own UUID before save() is ever called (see TemplateService),
      // so there's no null-id branch to gate on here.
      Template t = inv.getArgument(0);
      savedTemplate = t;
      return t;
    });
    when(templateMetaRepository.save(any(TemplateMeta.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  @DisplayName("landing page template is built and published while its promotion goes live")
  void createTemplatePublishAndActivatePromotionFlow() {
    when(templateRepository.existsBySlug("summer-sale")).thenReturn(false);

    TemplateDetail created = templateService.createTemplate(
        new CreateTemplateRequest("Summer Sale", "LANDING_PAGE", "summer-sale"), "admin-1");
    assertThat(created.status()).isEqualTo("DRAFT");
    java.util.UUID templateId = java.util.UUID.fromString(created.templateId());

    when(templateBlockRepository.save(any(TemplateBlock.class))).thenAnswer(inv -> inv.getArgument(0));
    var block = templateBlockService.addBlock(
        templateId,
        new AddBlockRequest("HERO", "Hero", 1, Map.of("title", "Summer Sale — Save 20% with SUMMER20")));
    assertThat(block.blockType()).isEqualTo("HERO");

    when(templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId)).thenReturn(List.of(blockEntity(templateId)));
    when(templateMetaRepository.findByTemplateId(templateId)).thenReturn(Optional.of(metaEntity(templateId)));
    when(templateRepository.findById(templateId)).thenReturn(Optional.of(savedTemplate));
    when(templateVersionRepository.countByTemplateId(templateId)).thenReturn(0);

    var published = templateService.publishTemplate(templateId, "Launch summer campaign", "admin-1");
    assertThat(published.status()).isEqualTo("PUBLISHED");

    // Publicly rendered content reflects the published template.
    when(templateRepository.findBySlug("summer-sale")).thenAnswer(inv -> Optional.of(savedTemplate));
    TemplateDetail publicView = templateService.getPublishedBySlug("summer-sale");
    assertThat(publicView.status()).isEqualTo("PUBLISHED");
    assertThat(publicView.blocks()).hasSize(1);
    assertThat(publicView.blocks().get(0).content()).containsEntry("title", "Summer Sale — Save 20% with SUMMER20");

    // The promotion referenced by the landing page copy goes live independently.
    when(promotionRepository.save(any(org.sirantar.recadero.promotions.domain.Promotion.class))).thenAnswer(inv -> inv.getArgument(0));

    CreatePromotionResponse promotion = promotionService.createPromotion(
        new CreatePromotionRequest(
            "Summer Sale 20%", "PERCENTAGE_DISCOUNT", BigDecimal.valueOf(20), null, null,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(14), 1000, 5, List.of()),
        "admin-1");

    assertThat(promotion.status()).isEqualTo("ACTIVE");
    assertThat(promotion.name()).isEqualTo("Summer Sale 20%");
  }

  private TemplateBlock blockEntity(java.util.UUID templateId) {
    TemplateBlock block = new TemplateBlock();
    block.setId(java.util.UUID.randomUUID());
    block.setTemplateId(templateId);
    block.setBlockType(org.sirantar.recadero.templates.domain.BlockType.HERO);
    block.setBlockName("Hero");
    block.setBlockOrder(1);
    block.setIsVisible(true);
    block.setContentJson("{\"title\":\"Summer Sale — Save 20% with SUMMER20\"}");
    block.setCreatedAt(LocalDateTime.now());
    block.setUpdatedAt(LocalDateTime.now());
    return block;
  }

  private TemplateMeta metaEntity(java.util.UUID templateId) {
    TemplateMeta meta = new TemplateMeta();
    meta.setId(java.util.UUID.randomUUID());
    meta.setTemplateId(templateId);
    meta.setUpdatedAt(LocalDateTime.now());
    return meta;
  }
}
