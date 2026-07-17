package org.sirantar.recadero.promotions.repository;

import java.util.List;
import org.sirantar.recadero.promotions.domain.PromotionUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for promotion/coupon redemption history.
 */
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

  Page<PromotionUsage> findByPromotionIdOrderByUsedAtDesc(Long promotionId, Pageable pageable);

  List<PromotionUsage> findByPromotionId(Long promotionId);

  long countByPromotionIdAndUserId(Long promotionId, Long userId);

  long countByUserId(Long userId);
}
