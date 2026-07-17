package org.sirantar.recadero.promotions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionRule;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.domain.RuleType;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.sirantar.recadero.promotions.service.dto.CouponSummary;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionRequest;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionResponse;
import org.sirantar.recadero.promotions.service.dto.PromotionDetail;
import org.sirantar.recadero.promotions.service.dto.PromotionRuleRequest;
import org.sirantar.recadero.promotions.service.dto.PromotionRuleResponse;
import org.sirantar.recadero.promotions.service.dto.PromotionSummary;
import org.sirantar.recadero.promotions.service.dto.UpdatePromotionRequest;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Promotion CRUD and lifecycle.
 */
@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionRuleRepository promotionRuleRepository;
  private final CouponCodeRepository couponCodeRepository;
  private final PromotionValidationService promotionValidationService;
  private final PromotionEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public CreatePromotionResponse createPromotion(CreatePromotionRequest request, String createdBy) {
    PromotionType type = PromotionType.valueOf(request.promotionType());
    promotionValidationService.validateDates(request.startDate(), request.endDate());
    promotionValidationService.validateDiscountValue(type, request.discountValue());
    promotionValidationService.validateUsageLimit(request.usageLimit());

    Promotion promotion = new Promotion();
    promotion.setName(request.name());
    promotion.setType(type);
    promotion.setDiscountValue(request.discountValue());
    promotion.setMaxDiscountAmount(request.maxDiscountAmount());
    promotion.setMinimumOrderAmount(request.minimumOrderAmount());
    promotion.setStartDate(request.startDate());
    promotion.setEndDate(request.endDate());
    promotion.setUsageLimit(request.usageLimit());
    promotion.setPriority(request.priority() != null ? request.priority() : 0);
    promotion.setStatus(resolveInitialStatus(request.startDate()));
    promotion.setCreatedBy(createdBy);
    LocalDateTime now = LocalDateTime.now();
    promotion.setCreatedAt(now);
    promotion.setUpdatedAt(now);
    Promotion saved = promotionRepository.save(promotion);

    if (request.rules() != null) {
      for (PromotionRuleRequest ruleRequest : request.rules()) {
        addRule(saved.getId(), ruleRequest);
      }
    }

    if (saved.getStatus() == PromotionStatus.ACTIVE) {
      eventPublisher.publishActivated(saved.getId(), saved.getName(), saved.getStartDate());
    }

    return new CreatePromotionResponse(saved.getId(), saved.getName(), saved.getStatus().name(), saved.getCreatedAt());
  }

  public PaginationResponse<PromotionSummary> listPromotions(
      PromotionStatus status, PromotionType type, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
    Page<Promotion> page = promotionRepository.search(status, type, fromDate, toDate, pageable);
    return PaginationResponse.from(page.map(this::toSummary));
  }

  public PromotionDetail getPromotion(Long promotionId) {
    return toDetail(getOrThrow(promotionId));
  }

  @Transactional
  public PromotionDetail updatePromotion(Long promotionId, UpdatePromotionRequest request) {
    Promotion promotion = getOrThrow(promotionId);
    if (request.name() != null) promotion.setName(request.name());
    if (request.discountValue() != null) promotion.setDiscountValue(request.discountValue());
    if (request.maxDiscountAmount() != null) promotion.setMaxDiscountAmount(request.maxDiscountAmount());
    if (request.minimumOrderAmount() != null) promotion.setMinimumOrderAmount(request.minimumOrderAmount());
    if (request.startDate() != null) promotion.setStartDate(request.startDate());
    if (request.endDate() != null) promotion.setEndDate(request.endDate());
    if (request.usageLimit() != null) promotion.setUsageLimit(request.usageLimit());
    if (request.priority() != null) promotion.setPriority(request.priority());
    promotionValidationService.validateDates(promotion.getStartDate(), promotion.getEndDate());
    promotion.setUpdatedAt(LocalDateTime.now());
    return toDetail(promotionRepository.save(promotion));
  }

  @Transactional
  public PromotionDetail changeStatus(Long promotionId, PromotionStatus newStatus) {
    Promotion promotion = getOrThrow(promotionId);
    if (newStatus == PromotionStatus.ACTIVE && promotion.getStatus() == PromotionStatus.EXPIRED) {
      throw new ResourceConflictException(
          "INVALID_PROMOTION_STATE",
          "Cannot activate promotion that has already expired",
          Map.of("promotion_status", promotion.getStatus().name(), "end_date", promotion.getEndDate()));
    }
    promotion.setStatus(newStatus);
    promotion.setUpdatedAt(LocalDateTime.now());
    Promotion saved = promotionRepository.save(promotion);
    if (newStatus == PromotionStatus.ACTIVE) {
      eventPublisher.publishActivated(saved.getId(), saved.getName(), saved.getStartDate());
    }
    return toDetail(saved);
  }

  @Transactional
  public void archivePromotion(Long promotionId) {
    Promotion promotion = getOrThrow(promotionId);
    promotion.setStatus(PromotionStatus.ARCHIVED);
    promotion.setUpdatedAt(LocalDateTime.now());
    promotionRepository.save(promotion);
  }

  @Transactional
  public PromotionRuleResponse addRule(Long promotionId, PromotionRuleRequest request) {
    getOrThrow(promotionId);
    PromotionRule rule = new PromotionRule();
    rule.setPromotionId(promotionId);
    rule.setRuleType(RuleType.valueOf(request.ruleType()));
    rule.setConditionJson(writeJson(request.conditionJson()));
    rule.setCreatedAt(LocalDateTime.now());
    PromotionRule saved = promotionRuleRepository.save(rule);
    return new PromotionRuleResponse(saved.getId(), promotionId, saved.getRuleType().name(), request.conditionJson());
  }

  @Transactional
  public void removeRule(Long promotionId, Long ruleId) {
    promotionRuleRepository.deleteByIdAndPromotionId(ruleId, promotionId);
  }

  private PromotionStatus resolveInitialStatus(LocalDateTime startDate) {
    return startDate != null && !startDate.isAfter(LocalDateTime.now()) ? PromotionStatus.ACTIVE : PromotionStatus.DRAFT;
  }

  private Promotion getOrThrow(Long promotionId) {
    return promotionRepository.findById(promotionId)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionId));
  }

  private PromotionSummary toSummary(Promotion promotion) {
    return new PromotionSummary(
        promotion.getId(),
        promotion.getName(),
        promotion.getType().name(),
        promotion.getDiscountValue(),
        promotion.getStatus().name(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getUsageLimit(),
        promotion.getCurrentUsageCount(),
        promotion.getPriority(),
        promotion.getCreatedBy(),
        promotion.getCreatedAt());
  }

  private PromotionDetail toDetail(Promotion promotion) {
    List<PromotionRuleResponse> rules = promotionRuleRepository.findByPromotionId(promotion.getId()).stream()
        .map(r -> new PromotionRuleResponse(r.getId(), promotion.getId(), r.getRuleType().name(), readJson(r.getConditionJson())))
        .toList();
    List<CouponSummary> coupons = couponCodeRepository.findByPromotionId(promotion.getId(), Pageable.unpaged()).stream()
        .map(c -> new CouponSummary(c.getId(), c.getCode(), c.getUsageLimit(), c.getCurrentUsage(), Boolean.TRUE.equals(c.getIsActive()), c.getExpiryDate(), c.getCreatedAt()))
        .toList();

    return new PromotionDetail(
        promotion.getId(),
        promotion.getName(),
        promotion.getType().name(),
        promotion.getDiscountValue(),
        promotion.getMaxDiscountAmount(),
        promotion.getMinimumOrderAmount(),
        promotion.getStatus().name(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getUsageLimit(),
        promotion.getCurrentUsageCount(),
        promotion.getPriority(),
        rules,
        coupons,
        promotion.getCreatedBy(),
        promotion.getCreatedAt());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readJson(String json) {
    try {
      return objectMapper.readValue(json == null || json.isBlank() ? "{}" : json, Map.class);
    } catch (JsonProcessingException e) {
      return Map.of();
    }
  }

  private String writeJson(Map<String, Object> value) {
    try {
      return objectMapper.writeValueAsString(value != null ? value : Map.of());
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid condition_json", e);
    }
  }
}
