package org.sirantar.recadero.promotions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.springframework.stereotype.Service;

/**
 * Structural validation for promotion creation/updates.
 */
@Service
public class PromotionValidationService {

  public void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("start_date is required");
    }
    if (endDate != null && !endDate.isAfter(startDate)) {
      throw new IllegalArgumentException("end_date must be after start_date");
    }
  }

  public void validateDiscountValue(PromotionType type, BigDecimal discountValue) {
    if (type == PromotionType.FREE_SHIPPING || type == PromotionType.BOGO) {
      return;
    }
    if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("discount_value must be positive for " + type);
    }
    if (type == PromotionType.PERCENTAGE_DISCOUNT && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new IllegalArgumentException("discount_value must be <= 100 for PERCENTAGE_DISCOUNT");
    }
  }

  public void validateUsageLimit(Integer usageLimit) {
    if (usageLimit != null && usageLimit <= 0) {
      throw new IllegalArgumentException("usage_limit must be positive if set");
    }
  }
}
