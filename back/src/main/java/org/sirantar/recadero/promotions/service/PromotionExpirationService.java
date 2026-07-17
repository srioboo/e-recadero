package org.sirantar.recadero.promotions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically auto-expires promotions that have passed their end date.
 */
@Service
@RequiredArgsConstructor
public class PromotionExpirationService {

  private static final Logger log = LoggerFactory.getLogger(PromotionExpirationService.class);
  private static final long ONE_HOUR_MS = 3_600_000L;

  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final PromotionEventPublisher eventPublisher;

  @Scheduled(fixedRate = ONE_HOUR_MS)
  @Transactional
  public void expirePromotions() {
    LocalDateTime now = LocalDateTime.now();
    List<Promotion> toExpire = promotionRepository.findByStatusAndEndDateBefore(PromotionStatus.ACTIVE, now);

    for (Promotion promotion : toExpire) {
      promotion.setStatus(PromotionStatus.EXPIRED);
      promotion.setUpdatedAt(now);
      promotionRepository.save(promotion);

      BigDecimal totalDiscount = promotionUsageRepository.findByPromotionId(promotion.getId()).stream()
          .map(u -> u.getDiscountAmount() != null ? u.getDiscountAmount() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      eventPublisher.publishExpired(promotion.getId(), promotion.getName(), promotion.getCurrentUsageCount(), totalDiscount);
    }

    if (!toExpire.isEmpty()) {
      log.info("Auto-expired {} promotion(s)", toExpire.size());
    }
  }
}
