package org.sirantar.recadero.templates.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.templates.domain.BlockType;
import org.sirantar.recadero.templates.domain.TemplateBlock;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.ReorderBlocksRequest;
import org.sirantar.recadero.templates.service.dto.TemplateBlockResponse;

@ExtendWith(MockitoExtension.class)
class TemplateBlockServiceTest {

  @Mock private TemplateBlockRepository templateBlockRepository;

  private TemplateBlockService blockService;
  private UUID templateId;

  @BeforeEach
  void setUp() {
    blockService = new TemplateBlockService(templateBlockRepository, new ObjectMapper());
    templateId = UUID.randomUUID();
  }

  @Test
  void addBlockAcceptsValidHeroContent() {
    when(templateBlockRepository.save(any(TemplateBlock.class))).thenAnswer(inv -> inv.getArgument(0));

    AddBlockRequest request = new AddBlockRequest("HERO", "Main hero", 1, Map.of("title", "Welcome"));
    TemplateBlockResponse response = blockService.addBlock(templateId, request);

    assertThat(response.blockType()).isEqualTo("HERO");
    assertThat(response.content()).containsEntry("title", "Welcome");
  }

  @Test
  void addBlockRejectsHeroContentMissingTitle() {
    AddBlockRequest request = new AddBlockRequest("HERO", "Main hero", 1, Map.of("subtitle", "no title here"));

    assertThatThrownBy(() -> blockService.addBlock(templateId, request))
        .isInstanceOf(BusinessLogicException.class)
        .satisfies(ex -> assertThat(((BusinessLogicException) ex).getErrorCode()).isEqualTo("INVALID_BLOCK_CONTENT"));
  }

  @Test
  void addBlockRejectsProductGridWithEmptyProductsList() {
    AddBlockRequest request = new AddBlockRequest("PRODUCT_GRID", "Grid", 1, Map.of("products", List.of()));

    assertThatThrownBy(() -> blockService.addBlock(templateId, request))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void addBlockAllowsHeaderWithNoRequiredFields() {
    when(templateBlockRepository.save(any(TemplateBlock.class))).thenAnswer(inv -> inv.getArgument(0));

    AddBlockRequest request = new AddBlockRequest("HEADER", "Site header", 1, Map.of());
    TemplateBlockResponse response = blockService.addBlock(templateId, request);

    assertThat(response.blockType()).isEqualTo("HEADER");
  }

  @Test
  void addBlockRejectsUnknownBlockType() {
    AddBlockRequest request = new AddBlockRequest("NOT_A_TYPE", "x", 1, Map.of());

    assertThatThrownBy(() -> blockService.addBlock(templateId, request))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void reorderBlocksMaintainsOrderConsistency() {
    TemplateBlock hero = block(BlockType.HERO, 1);
    TemplateBlock cta = block(BlockType.CTA, 2);
    TemplateBlock richText = block(BlockType.RICH_TEXT, 3);

    when(templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId))
        .thenReturn(new java.util.ArrayList<>(List.of(hero, cta, richText)));

    ReorderBlocksRequest request = new ReorderBlocksRequest(List.of(
        new ReorderBlocksRequest.Entry(hero.getId().toString(), 1),
        new ReorderBlocksRequest.Entry(richText.getId().toString(), 2),
        new ReorderBlocksRequest.Entry(cta.getId().toString(), 3)));

    blockService.reorderBlocks(templateId, request);

    assertThat(hero.getBlockOrder()).isEqualTo(1);
    assertThat(richText.getBlockOrder()).isEqualTo(2);
    assertThat(cta.getBlockOrder()).isEqualTo(3);
  }

  private TemplateBlock block(BlockType type, int order) {
    TemplateBlock block = new TemplateBlock();
    block.setId(UUID.randomUUID());
    block.setTemplateId(templateId);
    block.setBlockType(type);
    block.setBlockOrder(order);
    block.setIsVisible(true);
    block.setContentJson("{}");
    block.setCreatedAt(LocalDateTime.now());
    block.setUpdatedAt(LocalDateTime.now());
    return block;
  }
}
