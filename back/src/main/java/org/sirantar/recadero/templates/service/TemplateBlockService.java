package org.sirantar.recadero.templates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.templates.domain.BlockType;
import org.sirantar.recadero.templates.domain.TemplateBlock;
import org.sirantar.recadero.templates.repository.TemplateBlockRepository;
import org.sirantar.recadero.templates.service.dto.AddBlockRequest;
import org.sirantar.recadero.templates.service.dto.ReorderBlocksRequest;
import org.sirantar.recadero.templates.service.dto.TemplateBlockResponse;
import org.sirantar.recadero.templates.service.dto.UpdateBlockRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages a template's ordered content blocks, including structural
 * per-block-type content validation (required fields only — cross-module
 * existence checks against Catalog product/category IDs are deliberately
 * out of scope here: front already treats unresolved IDs as absent at
 * render time, see front/src/lib/templates.ts).
 */
@Service
@RequiredArgsConstructor
public class TemplateBlockService {

  private static final Map<BlockType, List<String>> REQUIRED_FIELDS = Map.of(
      BlockType.HERO, List.of("title"),
      BlockType.PRODUCT_GRID, List.of("products"),
      BlockType.CATEGORY_LIST, List.of("category_ids"),
      BlockType.FEATURED_PRODUCTS, List.of("products"),
      BlockType.RICH_TEXT, List.of("html_content"),
      BlockType.IMAGE_BANNER, List.of("image_url"),
      BlockType.TESTIMONIALS, List.of("testimonials"),
      BlockType.CTA, List.of("text", "link"),
      BlockType.HEADER, List.of(),
      BlockType.FOOTER, List.of());

  private final TemplateBlockRepository templateBlockRepository;
  private final ObjectMapper objectMapper;

  public List<TemplateBlockResponse> listBlocks(UUID templateId) {
    return templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public TemplateBlockResponse addBlock(UUID templateId, AddBlockRequest request) {
    BlockType blockType = parseBlockType(request.blockType());
    validateContent(blockType, request.content());

    TemplateBlock block = new TemplateBlock();
    block.setId(UUID.randomUUID());
    block.setTemplateId(templateId);
    block.setBlockType(blockType);
    block.setBlockName(request.blockName());
    block.setBlockOrder(request.blockOrder() != null ? request.blockOrder() : nextOrder(templateId));
    block.setIsVisible(true);
    block.setContentJson(writeJson(request.content()));
    LocalDateTime now = LocalDateTime.now();
    block.setCreatedAt(now);
    block.setUpdatedAt(now);

    return toResponse(templateBlockRepository.save(block));
  }

  @Transactional
  public TemplateBlockResponse updateBlock(UUID templateId, UUID blockId, UpdateBlockRequest request) {
    TemplateBlock block = getOwnedBlock(templateId, blockId);

    if (request.content() != null) {
      Map<String, Object> merged = mergeContent(block, request.content());
      validateContent(block.getBlockType(), merged);
      block.setContentJson(writeJson(merged));
    }
    if (request.blockOrder() != null) {
      block.setBlockOrder(request.blockOrder());
    }
    block.setUpdatedAt(LocalDateTime.now());

    return toResponse(templateBlockRepository.save(block));
  }

  @Transactional
  public boolean setVisibility(UUID templateId, UUID blockId, boolean isVisible) {
    TemplateBlock block = getOwnedBlock(templateId, blockId);
    block.setIsVisible(isVisible);
    block.setUpdatedAt(LocalDateTime.now());
    templateBlockRepository.save(block);
    return block.getIsVisible();
  }

  @Transactional
  public void deleteBlock(UUID templateId, UUID blockId) {
    TemplateBlock block = getOwnedBlock(templateId, blockId);
    templateBlockRepository.delete(block);
  }

  @Transactional
  public void reorderBlocks(UUID templateId, ReorderBlocksRequest request) {
    List<TemplateBlock> blocks = templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId);
    for (ReorderBlocksRequest.Entry entry : request.blocks()) {
      blocks.stream()
          .filter(b -> b.getId().equals(UUID.fromString(entry.blockId())))
          .findFirst()
          .ifPresent(b -> {
            b.setBlockOrder(entry.blockOrder());
            b.setUpdatedAt(LocalDateTime.now());
          });
    }
    templateBlockRepository.saveAll(blocks);
  }

  private TemplateBlock getOwnedBlock(UUID templateId, UUID blockId) {
    TemplateBlock block = templateBlockRepository.findById(blockId)
        .orElseThrow(() -> new ResourceNotFoundException("Block not found: " + blockId));
    if (!block.getTemplateId().equals(templateId)) {
      throw new ResourceNotFoundException("Block not found: " + blockId);
    }
    return block;
  }

  private int nextOrder(UUID templateId) {
    return templateBlockRepository.findByTemplateIdOrderByBlockOrder(templateId).size() + 1;
  }

  private BlockType parseBlockType(String value) {
    try {
      return BlockType.valueOf(value);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BusinessLogicException(
          "INVALID_BLOCK_CONTENT", "Unknown block type: " + value, Map.of("block_type", String.valueOf(value)));
    }
  }

  private void validateContent(BlockType blockType, Map<String, Object> content) {
    List<String> required = REQUIRED_FIELDS.getOrDefault(blockType, List.of());
    List<Map<String, String>> errors = required.stream()
        .filter(field -> isBlank(content == null ? null : content.get(field)))
        .map(field -> Map.of("field", field, "issue", "This field is required for " + blockType + " blocks"))
        .toList();

    if (!errors.isEmpty()) {
      throw new BusinessLogicException(
          "INVALID_BLOCK_CONTENT",
          "Block content validation failed",
          Map.of("block_type", blockType.name(), "errors", errors));
    }
  }

  private boolean isBlank(Object value) {
    if (value == null) return true;
    if (value instanceof String s) return s.isBlank();
    if (value instanceof List<?> list) return list.isEmpty();
    return false;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> mergeContent(TemplateBlock block, Map<String, Object> patch) {
    Map<String, Object> current = new java.util.HashMap<>(readJson(block.getContentJson()));
    current.putAll(patch);
    return current;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readJson(String json) {
    if (json == null || json.isBlank()) {
      return new java.util.HashMap<>();
    }
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (JsonProcessingException e) {
      return new java.util.HashMap<>();
    }
  }

  private String writeJson(Map<String, Object> value) {
    try {
      return objectMapper.writeValueAsString(value != null ? value : Map.of());
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid block content", e);
    }
  }

  TemplateBlockResponse toResponse(TemplateBlock block) {
    return new TemplateBlockResponse(
        block.getId().toString(),
        block.getTemplateId().toString(),
        block.getBlockType().name(),
        block.getBlockName(),
        block.getBlockOrder(),
        Boolean.TRUE.equals(block.getIsVisible()),
        readJson(block.getContentJson()),
        block.getCreatedAt());
  }
}
