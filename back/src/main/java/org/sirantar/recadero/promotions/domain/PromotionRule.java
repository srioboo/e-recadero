package org.sirantar.recadero.promotions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single eligibility condition on a promotion (e.g. "only these
 * categories", "only new customers"). {@link #conditionJson} holds a
 * shape specific to {@link #ruleType}, parsed by PromotionRulesEngine.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion_rule", schema = "promotions")
public class PromotionRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "rule_type", nullable = false, length = 100)
  private RuleType ruleType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "condition_data", nullable = false, columnDefinition = "jsonb")
  private String conditionJson = "{}";

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
