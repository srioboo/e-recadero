package org.sirantar.recadero.promotions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionRule;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.domain.RuleMatchMode;
import org.sirantar.recadero.promotions.domain.RuleType;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PromotionRulesEngineTest {

  @Mock private PromotionRuleRepository promotionRuleRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private OrderRepository orderRepository;

  private PromotionRulesEngine rulesEngine;
  private Promotion promotion;

  @BeforeEach
  void setUp() {
    rulesEngine = new PromotionRulesEngine(promotionRuleRepository, productVariantRepository, orderRepository, new ObjectMapper());

    promotion = new Promotion();
    promotion.setId(1L);
    promotion.setType(PromotionType.PERCENTAGE_DISCOUNT);
    promotion.setDiscountValue(BigDecimal.valueOf(50));
    promotion.setRuleMatchMode(RuleMatchMode.ALL);
  }

  @Test
  void evaluatePromotionReturnsTrueWhenNoRulesConfigured() {
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of());

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(50L), 10L)).isTrue();
  }

  @Test
  void evaluatePromotionMatchesProductIncludeRule() {
    PromotionRule rule = rule(RuleType.PRODUCT_INCLUDE, "{\"product_ids\":[100]}");
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of(rule));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant(100L, null)));

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(50L), 10L)).isTrue();
  }

  @Test
  void evaluatePromotionRejectsProductNotInIncludeList() {
    PromotionRule rule = rule(RuleType.PRODUCT_INCLUDE, "{\"product_ids\":[999]}");
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of(rule));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant(100L, null)));

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(50L), 10L)).isFalse();
  }

  @Test
  void evaluatePromotionMatchesNewCustomerOnlyForUserWithNoOrders() {
    PromotionRule rule = rule(RuleType.NEW_CUSTOMER_ONLY, "{}");
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of(rule));
    when(orderRepository.findByUserId(any(), any())).thenReturn(Page.empty());

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(), 10L)).isTrue();
  }

  @Test
  void evaluatePromotionRejectsNewCustomerOnlyForReturningCustomer() {
    PromotionRule rule = rule(RuleType.NEW_CUSTOMER_ONLY, "{}");
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of(rule));
    when(orderRepository.findByUserId(any(), any()))
        .thenReturn(new PageImpl<>(List.of(new org.sirantar.recadero.orders.domain.Order())));

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(), 10L)).isFalse();
  }

  @Test
  void evaluatePromotionWithAnyMatchModeSucceedsIfOneRulePasses() {
    promotion.setRuleMatchMode(RuleMatchMode.ANY);
    PromotionRule passingRule = rule(RuleType.PRODUCT_INCLUDE, "{\"product_ids\":[100]}");
    PromotionRule failingRule = rule(RuleType.PRODUCT_INCLUDE, "{\"product_ids\":[999]}");
    when(promotionRuleRepository.findByPromotionId(1L)).thenReturn(List.of(failingRule, passingRule));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant(100L, null)));

    assertThat(rulesEngine.evaluatePromotion(promotion, List.of(50L), 10L)).isTrue();
  }

  @Test
  void calculateDiscountAppliesMaxDiscountAmountCap() {
    promotion.setMaxDiscountAmount(BigDecimal.valueOf(20));

    BigDecimal discount = rulesEngine.calculateDiscount(promotion, BigDecimal.valueOf(100));

    assertThat(discount).isEqualByComparingTo("20");
  }

  @Test
  void calculateDiscountNeverExceedsSubtotal() {
    promotion.setType(PromotionType.FIXED_DISCOUNT);
    promotion.setDiscountValue(BigDecimal.valueOf(500));

    BigDecimal discount = rulesEngine.calculateDiscount(promotion, BigDecimal.valueOf(30));

    assertThat(discount).isEqualByComparingTo("30");
  }

  private PromotionRule rule(RuleType type, String conditionJson) {
    PromotionRule rule = new PromotionRule();
    rule.setId(1L);
    rule.setPromotionId(1L);
    rule.setRuleType(type);
    rule.setConditionJson(conditionJson);
    rule.setCreatedAt(LocalDateTime.now());
    return rule;
  }

  private ProductVariant variant(Long productId, Long categoryId) {
    Product product = new Product();
    product.setId(productId);
    if (categoryId != null) {
      Category category = new Category();
      category.setId(categoryId);
      product.setCategory(category);
    }
    ProductVariant variant = new ProductVariant();
    variant.setId(50L);
    variant.setProduct(product);
    return variant;
  }
}
