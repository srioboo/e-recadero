package org.sirantar.recadero.promotions.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionUsage;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.sirantar.recadero.promotions.service.dto.AnalyticsResponse;
import org.sirantar.recadero.promotions.service.dto.UsageListItem;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Usage-derived metrics for a promotion. {@code top_products} in
 * {@link AnalyticsResponse} is always empty — PromotionUsage tracks
 * redemptions per order, not per product line item.
 */
@Service
@RequiredArgsConstructor
public class PromotionAnalyticsService {

  private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final CouponCodeRepository couponCodeRepository;

  public AnalyticsResponse getAnalytics(Long promotionId) {
    Promotion promotion = promotionRepository.findById(promotionId)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionId));
    List<PromotionUsage> usages = promotionUsageRepository.findByPromotionId(promotionId);

    long totalOrders = usages.stream().map(PromotionUsage::getOrderId).filter(java.util.Objects::nonNull).distinct().count();
    long uniqueUsers = usages.stream().map(PromotionUsage::getUserId).distinct().count();
    BigDecimal totalDiscount = usages.stream()
        .map(u -> u.getDiscountAmount() != null ? u.getDiscountAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal averageDiscount = totalOrders > 0
        ? totalDiscount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;
    String usageRate = promotion.getUsageLimit() != null && promotion.getUsageLimit() > 0
        ? (promotion.getCurrentUsageCount() * 100 / promotion.getUsageLimit()) + "%"
        : "N/A";

    Map<String, long[]> ordersByDay = new LinkedHashMap<>();
    Map<String, BigDecimal> discountByDay = new LinkedHashMap<>();
    for (PromotionUsage usage : usages) {
      String day = usage.getUsedAt().toLocalDate().format(DAY_FORMAT);
      ordersByDay.merge(day, new long[] {1}, (a, b) -> new long[] {a[0] + 1});
      discountByDay.merge(day, usage.getDiscountAmount() != null ? usage.getDiscountAmount() : BigDecimal.ZERO, BigDecimal::add);
    }
    List<AnalyticsResponse.DailyBreakdown> dailyBreakdown = ordersByDay.keySet().stream()
        .sorted()
        .map(day -> new AnalyticsResponse.DailyBreakdown(day, ordersByDay.get(day)[0], discountByDay.get(day)))
        .toList();

    return new AnalyticsResponse(
        promotion.getId(),
        promotion.getName(),
        new AnalyticsResponse.Metrics(totalOrders, uniqueUsers, totalDiscount, averageDiscount, usageRate),
        dailyBreakdown,
        List.of());
  }

  public PaginationResponse<UsageListItem> getUsageHistory(Long promotionId, Pageable pageable) {
    Page<PromotionUsage> page = promotionUsageRepository.findByPromotionIdOrderByUsedAtDesc(promotionId, pageable);
    return PaginationResponse.from(page.map(u -> new UsageListItem(
        u.getId(), u.getOrderId(), u.getUserId(), resolveCouponCode(u.getCouponCodeId()), u.getDiscountAmount(), u.getUsedAt())));
  }

  private String resolveCouponCode(Long couponCodeId) {
    return couponCodeId == null ? null : couponCodeRepository.findById(couponCodeId).map(c -> c.getCode()).orElse(null);
  }
}
