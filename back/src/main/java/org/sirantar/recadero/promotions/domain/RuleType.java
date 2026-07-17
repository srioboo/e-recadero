package org.sirantar.recadero.promotions.domain;

/**
 * The kind of condition a {@link PromotionRule} evaluates.
 */
public enum RuleType {
  PRODUCT_INCLUDE,
  CATEGORY_INCLUDE,
  USER_SEGMENT,
  NEW_CUSTOMER_ONLY
}
