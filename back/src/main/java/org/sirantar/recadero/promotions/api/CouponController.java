package org.sirantar.recadero.promotions.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.service.CouponCodeService;
import org.sirantar.recadero.promotions.service.PromotionAnalyticsService;
import org.sirantar.recadero.promotions.service.dto.AnalyticsResponse;
import org.sirantar.recadero.promotions.service.dto.ApplyCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.promotions.service.dto.CouponSummary;
import org.sirantar.recadero.promotions.service.dto.UpdateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.UsageListItem;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponRequest;
import org.sirantar.recadero.promotions.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Coupon validation/application (Cart and Orders integration points) and
 * admin coupon/analytics views.
 * See specs/002-backend-ecommerce/contracts/promotions-contract.md.
 *
 * Note: in this monolith, Cart and Orders actually call {@link CouponCodeService}
 * directly as a same-JVM Spring bean (see cart.service.PromotionsCouponValidator
 * and orders.service.OrderService) rather than issuing HTTP requests to this
 * controller — these endpoints exist for contract completeness / external callers.
 */
@RestController
@RequiredArgsConstructor
public class CouponController {

  private final CouponCodeService couponCodeService;
  private final CouponCodeRepository couponCodeRepository;
  private final PromotionAnalyticsService promotionAnalyticsService;

  @PostMapping("/coupons/validate")
  public ValidateCouponResponse validate(@RequestBody ValidateCouponRequest request) {
    return couponCodeService.validateCoupon(request);
  }

  @PostMapping("/coupons/{code}/apply")
  public ApplyCouponResponse apply(@PathVariable String code, @RequestBody ApplyCouponRequest request) {
    return couponCodeService.applyCoupon(code, request.orderId(), request.userId(), request.discountAmount());
  }

  @AdminOnly
  @GetMapping("/promotions/{id}/coupons")
  public PaginationResponse<CouponSummary> listCoupons(
      @PathVariable Long id, @RequestParam(name = "is_active", required = false) Boolean isActive, Pageable pageable) {
    var page = couponCodeRepository.findByPromotionIdAndActive(id, isActive, pageable);
    return PaginationResponse.from(page.map(c -> new CouponSummary(
        c.getId(), c.getCode(), c.getUsageLimit(), c.getCurrentUsage(), Boolean.TRUE.equals(c.getIsActive()), c.getExpiryDate(), c.getCreatedAt())));
  }

  @AdminOnly
  @PutMapping("/promotions/{id}/coupons/{couponId}")
  public CouponSummary updateCoupon(
      @PathVariable Long id, @PathVariable Long couponId, @RequestBody UpdateCouponRequest request) {
    CouponCode coupon = couponCodeRepository.findById(couponId)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));
    if (request.isActive() != null) coupon.setIsActive(request.isActive());
    if (request.usageLimit() != null) coupon.setUsageLimit(request.usageLimit());
    CouponCode saved = couponCodeRepository.save(coupon);
    return new CouponSummary(
        saved.getId(), saved.getCode(), saved.getUsageLimit(), saved.getCurrentUsage(),
        Boolean.TRUE.equals(saved.getIsActive()), saved.getExpiryDate(), saved.getCreatedAt());
  }

  @AdminOnly
  @GetMapping("/promotions/{id}/analytics")
  public AnalyticsResponse analytics(@PathVariable Long id) {
    return promotionAnalyticsService.getAnalytics(id);
  }

  @AdminOnly
  @GetMapping("/promotions/{id}/usage")
  public PaginationResponse<UsageListItem> usage(@PathVariable Long id, Pageable pageable) {
    return promotionAnalyticsService.getUsageHistory(id, pageable);
  }
}
