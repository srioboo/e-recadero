package org.sirantar.recadero.promotions.api;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.service.CouponCodeService;
import org.sirantar.recadero.promotions.service.PromotionService;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionRequest;
import org.sirantar.recadero.promotions.service.dto.CreatePromotionResponse;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsRequest;
import org.sirantar.recadero.promotions.service.dto.GenerateCouponsResponse;
import org.sirantar.recadero.promotions.service.dto.PromotionDetail;
import org.sirantar.recadero.promotions.service.dto.PromotionRuleRequest;
import org.sirantar.recadero.promotions.service.dto.PromotionRuleResponse;
import org.sirantar.recadero.promotions.service.dto.PromotionSummary;
import org.sirantar.recadero.promotions.service.dto.StatusChangeRequest;
import org.sirantar.recadero.promotions.service.dto.UpdatePromotionRequest;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only promotion CRUD, rules, and coupon bulk generation.
 * See specs/002-backend-ecommerce/contracts/promotions-contract.md.
 */
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@AdminOnly
public class PromotionAdminController {

  private final PromotionService promotionService;
  private final CouponCodeService couponCodeService;

  @GetMapping
  public PaginationResponse<PromotionSummary> listPromotions(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(name = "from_date", required = false) LocalDateTime fromDate,
      @RequestParam(name = "to_date", required = false) LocalDateTime toDate,
      Pageable pageable) {
    return promotionService.listPromotions(
        status != null ? PromotionStatus.valueOf(status) : null,
        type != null ? PromotionType.valueOf(type) : null,
        fromDate, toDate, pageable);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreatePromotionResponse createPromotion(
      @RequestBody CreatePromotionRequest request, @AuthenticationPrincipal SecurityUser admin) {
    return promotionService.createPromotion(request, admin.getUserId());
  }

  @GetMapping("/{id}")
  public PromotionDetail getPromotion(@PathVariable Long id) {
    return promotionService.getPromotion(id);
  }

  @PutMapping("/{id}")
  public PromotionDetail updatePromotion(@PathVariable Long id, @RequestBody UpdatePromotionRequest request) {
    return promotionService.updatePromotion(id, request);
  }

  @PatchMapping("/{id}/status")
  public PromotionDetail changeStatus(@PathVariable Long id, @RequestBody StatusChangeRequest request) {
    return promotionService.changeStatus(id, PromotionStatus.valueOf(request.status()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void archivePromotion(@PathVariable Long id) {
    promotionService.archivePromotion(id);
  }

  @PostMapping("/{id}/rules")
  @ResponseStatus(HttpStatus.CREATED)
  public PromotionRuleResponse addRule(@PathVariable Long id, @RequestBody PromotionRuleRequest request) {
    return promotionService.addRule(id, request);
  }

  @DeleteMapping("/{id}/rules/{ruleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeRule(@PathVariable Long id, @PathVariable Long ruleId) {
    promotionService.removeRule(id, ruleId);
  }

  @PostMapping("/{id}/coupons")
  @ResponseStatus(HttpStatus.CREATED)
  public GenerateCouponsResponse generateCoupons(@PathVariable Long id, @RequestBody GenerateCouponsRequest request) {
    return couponCodeService.generateCoupons(id, request);
  }
}
