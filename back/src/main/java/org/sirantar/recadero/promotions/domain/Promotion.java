package org.sirantar.recadero.promotions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A discount campaign: percentage/fixed discount, free shipping, or BOGO,
 * gated by an optional coupon code and {@link PromotionRule}s.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion", schema = "promotions")
public class Promotion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "promotion_name", nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "promotion_type", nullable = false, length = 50)
  private PromotionType type;

  @Column(name = "discount_value", precision = 10, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "max_discount_amount", precision = 12, scale = 2)
  private BigDecimal maxDiscountAmount;

  @Column(name = "minimum_order_amount", precision = 12, scale = 2)
  private BigDecimal minimumOrderAmount;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "max_uses")
  private Integer usageLimit;

  @Column(name = "usage_count", nullable = false)
  private Integer currentUsageCount = 0;

  @Column(nullable = false)
  private Integer priority = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PromotionStatus status = PromotionStatus.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(name = "rule_match_mode", nullable = false, length = 10)
  private RuleMatchMode ruleMatchMode = RuleMatchMode.ALL;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  private Long version;
}
