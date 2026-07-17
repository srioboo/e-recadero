package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for GET /api/v1/promotions/{id}/analytics.
 * {@code top_products} is always empty: usage records aren't tracked at
 * per-product granularity (see PromotionUsage), only per-order.
 */
public record AnalyticsResponse(
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("promotion_name") String promotionName,
    Metrics metrics,
    @JsonProperty("daily_breakdown") List<DailyBreakdown> dailyBreakdown,
    @JsonProperty("top_products") List<Object> topProducts) {

  public record Metrics(
      @JsonProperty("total_orders") long totalOrders,
      @JsonProperty("unique_users") long uniqueUsers,
      @JsonProperty("total_discount_amount") BigDecimal totalDiscountAmount,
      @JsonProperty("average_discount_per_order") BigDecimal averageDiscountPerOrder,
      @JsonProperty("usage_rate") String usageRate) {}

  public record DailyBreakdown(String date, long orders, @JsonProperty("discount_amount") BigDecimal discountAmount) {}
}
