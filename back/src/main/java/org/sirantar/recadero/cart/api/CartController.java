package org.sirantar.recadero.cart.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.service.CartPromotionService;
import org.sirantar.recadero.cart.service.CartService;
import org.sirantar.recadero.cart.service.CheckoutService;
import org.sirantar.recadero.cart.service.dto.AddItemRequest;
import org.sirantar.recadero.cart.service.dto.AddItemResponse;
import org.sirantar.recadero.cart.service.dto.ApplyCouponRequest;
import org.sirantar.recadero.cart.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.cart.service.dto.CartHistoryItem;
import org.sirantar.recadero.cart.service.dto.CartResponse;
import org.sirantar.recadero.cart.service.dto.ClearCartResponse;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutResponse;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutResponse;
import org.sirantar.recadero.cart.service.dto.RecoverCartResponse;
import org.sirantar.recadero.cart.service.dto.RemoveCouponResponse;
import org.sirantar.recadero.cart.service.dto.UpdateItemRequest;
import org.sirantar.recadero.cart.service.dto.UpdateItemResponse;
import org.sirantar.recadero.cart.service.dto.ValidateCouponResponse;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.Authenticated;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated customer's shopping cart: items, coupons, and checkout.
 * See specs/002-backend-ecommerce/contracts/cart-contract.md.
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Authenticated
public class CartController {

  private final CartService cartService;
  private final CartPromotionService cartPromotionService;
  private final CheckoutService checkoutService;

  @GetMapping
  public CartResponse getCart(@AuthenticationPrincipal SecurityUser user) {
    return cartService.getCart(userId(user));
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public AddItemResponse addItem(@AuthenticationPrincipal SecurityUser user, @RequestBody AddItemRequest request) {
    return cartService.addItem(userId(user), request);
  }

  @PutMapping("/items/{id}")
  public UpdateItemResponse updateItem(
      @AuthenticationPrincipal SecurityUser user, @PathVariable Long id, @RequestBody UpdateItemRequest request) {
    return cartService.updateItemQuantity(userId(user), id, request.quantity());
  }

  @DeleteMapping("/items/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    cartService.removeItem(userId(user), id);
  }

  @PostMapping("/clear")
  public ClearCartResponse clearCart(@AuthenticationPrincipal SecurityUser user) {
    return cartService.clearCart(userId(user));
  }

  @PostMapping("/apply-coupon")
  public ApplyCouponResponse applyCoupon(
      @AuthenticationPrincipal SecurityUser user, @RequestBody ApplyCouponRequest request) {
    return cartPromotionService.applyCoupon(userId(user), request.couponCode());
  }

  @DeleteMapping("/remove-coupon")
  public RemoveCouponResponse removeCoupon(@AuthenticationPrincipal SecurityUser user) {
    return cartPromotionService.removeCoupon(userId(user));
  }

  @GetMapping("/validate-coupon")
  public ValidateCouponResponse validateCoupon(
      @AuthenticationPrincipal SecurityUser user, @RequestParam("coupon_code") String couponCode) {
    return cartPromotionService.validateCoupon(userId(user), couponCode);
  }

  @PostMapping("/prepare-checkout")
  public PrepareCheckoutResponse prepareCheckout(
      @AuthenticationPrincipal SecurityUser user, @RequestBody PrepareCheckoutRequest request) {
    return checkoutService.prepareCheckout(userId(user), request);
  }

  @PostMapping("/confirm-checkout")
  @ResponseStatus(HttpStatus.CREATED)
  public ConfirmCheckoutResponse confirmCheckout(
      @AuthenticationPrincipal SecurityUser user, @RequestBody ConfirmCheckoutRequest request) {
    return checkoutService.confirmCheckout(userId(user), request);
  }

  @GetMapping("/history")
  public PaginationResponse<CartHistoryItem> history(
      @AuthenticationPrincipal SecurityUser user,
      @RequestParam(required = false) String status,
      Pageable pageable) {
    return cartService.listHistory(userId(user), status != null ? CartStatus.valueOf(status) : null, pageable);
  }

  @PostMapping("/recover/{oldCartId}")
  public RecoverCartResponse recover(@AuthenticationPrincipal SecurityUser user, @PathVariable Long oldCartId) {
    return cartService.recoverCart(userId(user), oldCartId);
  }

  private Long userId(SecurityUser user) {
    return Long.valueOf(user.getUserId());
  }
}
