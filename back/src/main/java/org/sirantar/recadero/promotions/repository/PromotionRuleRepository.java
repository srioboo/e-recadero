package org.sirantar.recadero.promotions.repository;

import java.util.List;
import org.sirantar.recadero.promotions.domain.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for promotion eligibility rules.
 */
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

  List<PromotionRule> findByPromotionId(Long promotionId);

  void deleteByIdAndPromotionId(Long id, Long promotionId);
}
