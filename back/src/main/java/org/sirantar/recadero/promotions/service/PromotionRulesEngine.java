package org.sirantar.recadero.promotions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionRule;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.domain.RuleMatchMode;
import org.sirantar.recadero.promotions.domain.RuleType;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.springframework.stereotype.Service;

/**
 * Evaluates whether a promotion's rules are satisfied by a cart, and
 * computes the resulting discount.
 */
@Service
@RequiredArgsConstructor
public class PromotionRulesEngine {

  private final PromotionRuleRepository promotionRuleRepository;
  private final ProductVariantRepository productVariantRepository;
  private final OrderRepository orderRepository;
  private final ObjectMapper objectMapper;

  /** Whether {@code promotion} applies to a cart containing the given variants, for {@code userId}. */
  public boolean evaluatePromotion(Promotion promotion, List<Long> productVariantIds, Long userId) {
    List<PromotionRule> rules = promotionRuleRepository.findByPromotionId(promotion.getId());
    if (rules.isEmpty()) {
      return true;
    }

    boolean requireAll = promotion.getRuleMatchMode() == RuleMatchMode.ALL;
    for (PromotionRule rule : rules) {
      boolean matched = evaluateRule(rule, productVariantIds, userId);
      if (requireAll && !matched) {
        return false;
      }
      if (!requireAll && matched) {
        return true;
      }
    }
    return requireAll;
  }

  private boolean evaluateRule(PromotionRule rule, List<Long> productVariantIds, Long userId) {
    JsonNode condition = readCondition(rule.getConditionJson());
    return switch (rule.getRuleType()) {
      case PRODUCT_INCLUDE -> matchesAnyId(condition, "product_ids", resolveProductIds(productVariantIds));
      case CATEGORY_INCLUDE -> matchesAnyId(condition, "category_ids", resolveCategoryIds(productVariantIds));
      case USER_SEGMENT -> userId != null && matchesAnyId(condition, "user_ids", List.of(userId));
      case NEW_CUSTOMER_ONLY -> userId != null && orderRepository.findByUserId(userId, org.springframework.data.domain.Pageable.ofSize(1)).isEmpty();
    };
  }

  /** Discount for this promotion given a cart subtotal, respecting {@code max_discount_amount}. */
  public BigDecimal calculateDiscount(Promotion promotion, BigDecimal cartSubtotal) {
    if (cartSubtotal == null || cartSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal discount = switch (promotion.getType()) {
      case PERCENTAGE_DISCOUNT -> cartSubtotal
          .multiply(promotion.getDiscountValue())
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      case FIXED_DISCOUNT -> promotion.getDiscountValue();
      case FREE_SHIPPING, BOGO -> promotion.getDiscountValue() != null ? promotion.getDiscountValue() : BigDecimal.ZERO;
    };

    if (promotion.getMaxDiscountAmount() != null && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
      discount = promotion.getMaxDiscountAmount();
    }
    if (discount.compareTo(cartSubtotal) > 0) {
      discount = cartSubtotal;
    }
    return discount;
  }

  private List<Long> resolveProductIds(List<Long> productVariantIds) {
    return productVariantIds.stream()
        .map(id -> productVariantRepository.findById(id).map(ProductVariant::getProduct).map(p -> p.getId()).orElse(null))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private List<Long> resolveCategoryIds(List<Long> productVariantIds) {
    return productVariantIds.stream()
        .map(id -> productVariantRepository.findById(id)
            .map(ProductVariant::getProduct)
            .map(p -> p.getCategory() != null ? p.getCategory().getId() : null)
            .orElse(null))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private boolean matchesAnyId(JsonNode condition, String field, List<Long> candidateIds) {
    JsonNode idsNode = condition.get(field);
    if (idsNode == null || !idsNode.isArray()) {
      return false;
    }
    for (JsonNode idNode : idsNode) {
      if (candidateIds.contains(idNode.asLong())) {
        return true;
      }
    }
    return false;
  }

  private JsonNode readCondition(String json) {
    try {
      return objectMapper.readTree(json == null || json.isBlank() ? "{}" : json);
    } catch (JsonProcessingException e) {
      return objectMapper.createObjectNode();
    }
  }
}
