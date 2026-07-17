package org.sirantar.recadero.cart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartItem;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutResponse;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutResponse;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checkout preparation and confirmation. Tax/shipping calculation and actual
 * order creation are out of scope here: no rating/tax module exists yet
 * (so those totals are 0), and Orders (Phase 6) doesn't exist yet either —
 * confirmCheckout publishes {@link CheckoutCompletedEvent} for a future
 * Orders module to consume rather than creating an order synchronously.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

  private static final int CHECKOUT_HOLD_MINUTES = 30;

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final CartPromotionRepository cartPromotionRepository;
  private final ReservationRepository reservationRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CartService cartService;
  private final CartEventPublisher eventPublisher;

  @Transactional
  public PrepareCheckoutResponse prepareCheckout(Long userId, PrepareCheckoutRequest request) {
    Cart cart = cartService.getOrCreateCart(userId);
    List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
    if (items.isEmpty()) {
      throw new BusinessLogicException("EMPTY_CART", "Cannot checkout an empty cart");
    }

    List<Map<String, Object>> priceChanges = new java.util.ArrayList<>();
    for (CartItem item : items) {
      ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
      if (variant != null && variant.getPrice().compareTo(item.getPriceAtTime()) != 0) {
        priceChanges.add(Map.of(
            "product_variant_id", item.getProductVariantId(),
            "old_price", item.getPriceAtTime(),
            "new_price", variant.getPrice()));
      }
    }
    if (!priceChanges.isEmpty()) {
      throw new ResourceConflictException(
          "CART_MODIFIED",
          "Product prices changed since last update. Please review your cart.",
          Map.of("price_changes", priceChanges));
    }

    String checkoutToken = UUID.randomUUID().toString();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusMinutes(CHECKOUT_HOLD_MINUTES);

    cart.setStatus(CartStatus.LOCKED_FOR_CHECKOUT);
    cart.setCheckoutToken(checkoutToken);
    cart.setBillingAddressId(request.billingAddressId());
    cart.setShippingAddressId(request.shippingAddressId());
    cart.setShippingMethodId(request.shippingMethodId());
    cart.setUpdatedAt(now);
    cart.setExpiresAt(expiresAt);
    cartRepository.save(cart);

    var totals = cartService.calculateTotals(cart.getId());
    eventPublisher.publishCheckoutStarted(cart.getId(), userId, totals.grandTotal(), checkoutToken);

    return new PrepareCheckoutResponse(
        cart.getId(),
        cart.getStatus().name(),
        checkoutToken,
        totals,
        expiresAt,
        "Cart ready for checkout. Please complete payment within " + CHECKOUT_HOLD_MINUTES + " minutes.");
  }

  @Transactional
  public ConfirmCheckoutResponse confirmCheckout(
      Long userId, org.sirantar.recadero.cart.service.dto.ConfirmCheckoutRequest request) {
    Cart cart = cartService.getOrCreateCart(userId);
    if (cart.getStatus() != CartStatus.LOCKED_FOR_CHECKOUT
        || !java.util.Objects.equals(cart.getCheckoutToken(), request.checkoutToken())) {
      throw new BusinessLogicException("INVALID_CHECKOUT_TOKEN", "Checkout token is invalid or expired");
    }
    if (cart.getExpiresAt() != null && cart.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessLogicException("CHECKOUT_EXPIRED", "Checkout hold has expired. Please prepare checkout again.");
    }

    List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
    var totals = cartService.calculateTotals(cart.getId());
    var appliedPromotion = cartPromotionRepository.findByCartId(cart.getId()).stream().findFirst();

    List<CheckoutCompletedEvent.LineItem> eventItems = items.stream()
        .map(i -> new CheckoutCompletedEvent.LineItem(
            i.getProductVariantId(), i.getQuantity(), i.getPriceAtTime(), i.getDiscountApplied()))
        .toList();

    for (CartItem item : items) {
      reservationRepository.deleteByCartItemId(item.getId());
    }
    cartItemRepository.deleteByCartId(cart.getId());
    cartPromotionRepository.deleteByCartId(cart.getId());
    cart.setStatus(CartStatus.CHECKED_OUT);
    cart.setUpdatedAt(LocalDateTime.now());
    cartRepository.save(cart);

    eventPublisher.publishCheckoutCompleted(
        cart.getId(),
        userId,
        eventItems,
        cart.getBillingAddressId(),
        cart.getShippingAddressId(),
        cart.getShippingMethodId(),
        request.paymentMethod(),
        request.transactionId(),
        totals.grandTotal(),
        cart.getCheckoutToken(),
        appliedPromotion.map(org.sirantar.recadero.cart.domain.CartPromotion::getCouponCode).orElse(null),
        appliedPromotion.map(org.sirantar.recadero.cart.domain.CartPromotion::getDiscountAmount).orElse(null));

    // The order itself is created asynchronously by the Orders module's
    // CartCheckoutEventListener, after this transaction commits.
    return new ConfirmCheckoutResponse(
        null,
        null,
        cart.getId(),
        totals.grandTotal(),
        "Checkout received. Your order confirmation will be available shortly.",
        null);
  }
}
