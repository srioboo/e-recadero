package org.sirantar.recadero.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartItem;
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.domain.Reservation;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.dto.AddItemRequest;
import org.sirantar.recadero.cart.service.dto.AddItemResponse;
import org.sirantar.recadero.cart.service.dto.AppliedPromotion;
import org.sirantar.recadero.cart.service.dto.CartCalculations;
import org.sirantar.recadero.cart.service.dto.CartHistoryItem;
import org.sirantar.recadero.cart.service.dto.CartItemDetail;
import org.sirantar.recadero.cart.service.dto.CartResponse;
import org.sirantar.recadero.cart.service.dto.CartUpdatedSummary;
import org.sirantar.recadero.cart.service.dto.ClearCartResponse;
import org.sirantar.recadero.cart.service.dto.RecoverCartResponse;
import org.sirantar.recadero.cart.service.dto.UpdateItemResponse;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.catalog.service.OutOfStockException;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cart CRUD, item management, and total calculation.
 */
@Service
@RequiredArgsConstructor
public class CartService {

  private static final int RESERVATION_HOLD_HOURS = 24;

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final CartPromotionRepository cartPromotionRepository;
  private final ReservationRepository reservationRepository;
  private final ProductVariantRepository productVariantRepository;
  private final InventoryService inventoryService;
  private final CartValidationService cartValidationService;
  private final CartEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public Cart getOrCreateCart(Long userId) {
    return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseGet(() -> {
      Cart cart = new Cart();
      cart.setUserId(userId);
      cart.setStatus(CartStatus.ACTIVE);
      LocalDateTime now = LocalDateTime.now();
      cart.setCreatedAt(now);
      cart.setUpdatedAt(now);
      cart.setExpiresAt(now.plusHours(RESERVATION_HOLD_HOURS));
      return cartRepository.save(cart);
    });
  }

  public CartResponse getCart(Long userId) {
    return toResponse(getOrCreateCart(userId));
  }

  @Transactional
  public AddItemResponse addItem(Long userId, AddItemRequest request) {
    cartValidationService.validateQuantity(request.quantity());
    ProductVariant variant = cartValidationService.requireVariant(request.productVariantId());
    Cart cart = getOrCreateCart(userId);

    CartItem item = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), request.productVariantId())
        .orElse(null);
    int newQuantity = (item != null ? item.getQuantity() : 0) + request.quantity();
    cartValidationService.validateQuantity(newQuantity);

    reserveOrThrow(request.productVariantId(), request.quantity());

    LocalDateTime now = LocalDateTime.now();
    if (item == null) {
      item = new CartItem();
      item.setCartId(cart.getId());
      item.setProductVariantId(request.productVariantId());
      item.setDiscountApplied(BigDecimal.ZERO);
      item.setCreatedAt(now);
    }
    item.setQuantity(newQuantity);
    item.setPriceAtTime(variant.getPrice());
    item.setLineTotal(lineTotal(item));
    item.setUpdatedAt(now);
    CartItem saved = cartItemRepository.save(item);

    Reservation reservation = new Reservation();
    reservation.setCartItemId(saved.getId());
    reservation.setProductVariantId(request.productVariantId());
    reservation.setQuantity(request.quantity());
    reservation.setCreatedAt(now);
    reservation.setExpiresAt(now.plusHours(RESERVATION_HOLD_HOURS));
    reservationRepository.save(reservation);

    touchCart(cart);
    eventPublisher.publishItemAdded(cart.getId(), userId, request.productVariantId(), request.quantity(), variant.getPrice());

    CartCalculations totals = calculateTotals(cart.getId());
    return new AddItemResponse(
        saved.getId(),
        cart.getId(),
        saved.getProductVariantId(),
        saved.getQuantity(),
        saved.getPriceAtTime(),
        saved.getLineTotal(),
        "Item added to cart",
        CartUpdatedSummary.forItemChange(totalItemCount(cart.getId()), totals.grandTotal()));
  }

  @Transactional
  public UpdateItemResponse updateItemQuantity(Long userId, Long cartItemId, int newQuantity) {
    cartValidationService.validateQuantity(newQuantity);
    Cart cart = getOrCreateCart(userId);
    CartItem item = getOwnedItem(cart.getId(), cartItemId);

    int delta = newQuantity - item.getQuantity();
    if (delta > 0) {
      reserveOrThrow(item.getProductVariantId(), delta);
    } else if (delta < 0) {
      inventoryService.releaseReservation(item.getProductVariantId(), -delta);
    }

    item.setQuantity(newQuantity);
    item.setLineTotal(lineTotal(item));
    item.setUpdatedAt(LocalDateTime.now());
    CartItem saved = cartItemRepository.save(item);
    adjustReservation(saved.getId(), item.getProductVariantId(), delta);
    touchCart(cart);

    CartCalculations totals = calculateTotals(cart.getId());
    return new UpdateItemResponse(
        saved.getId(),
        saved.getQuantity(),
        saved.getLineTotal(),
        CartUpdatedSummary.forItemChange(totalItemCount(cart.getId()), totals.grandTotal()));
  }

  @Transactional
  public void removeItem(Long userId, Long cartItemId) {
    Cart cart = getOrCreateCart(userId);
    CartItem item = getOwnedItem(cart.getId(), cartItemId);

    inventoryService.releaseReservation(item.getProductVariantId(), item.getQuantity());
    reservationRepository.deleteByCartItemId(item.getId());
    cartItemRepository.delete(item);
    touchCart(cart);
  }

  @Transactional
  public ClearCartResponse clearCart(Long userId) {
    Cart cart = getOrCreateCart(userId);
    List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
    for (CartItem item : items) {
      inventoryService.releaseReservation(item.getProductVariantId(), item.getQuantity());
      reservationRepository.deleteByCartItemId(item.getId());
    }
    cartItemRepository.deleteByCartId(cart.getId());
    cartPromotionRepository.deleteByCartId(cart.getId());
    touchCart(cart);
    return new ClearCartResponse("Cart cleared successfully", items.size());
  }

  public CartCalculations calculateTotals(Long cartId) {
    List<CartItem> items = cartItemRepository.findByCartId(cartId);
    BigDecimal subtotal = items.stream().map(this::grossLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal discountTotal = cartPromotionRepository.findByCartId(cartId).stream()
        .map(CartPromotion::getDiscountAmount)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal taxTotal = BigDecimal.ZERO;
    BigDecimal shippingTotal = BigDecimal.ZERO;
    BigDecimal grandTotal = subtotal.subtract(discountTotal).add(taxTotal).add(shippingTotal);
    if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
      grandTotal = BigDecimal.ZERO;
    }
    return new CartCalculations(subtotal, discountTotal, taxTotal, shippingTotal, grandTotal);
  }

  public PaginationResponse<CartHistoryItem> listHistory(Long userId, CartStatus status, Pageable pageable) {
    List<CartStatus> statuses = status != null
        ? List.of(status)
        : List.of(CartStatus.CHECKED_OUT, CartStatus.ABANDONED);
    Page<CartHistoryItem> page = cartRepository.findByUserIdAndStatusIn(userId, statuses, pageable)
        .map(cart -> new CartHistoryItem(
            cart.getId(),
            cartItemRepository.countByCartId(cart.getId()),
            calculateTotals(cart.getId()).grandTotal(),
            cart.getStatus().name(),
            cart.getCreatedAt(),
            cart.getExpiresAt()));
    return PaginationResponse.from(page);
  }

  @Transactional
  public RecoverCartResponse recoverCart(Long userId, Long oldCartId) {
    Cart oldCart = cartRepository.findById(oldCartId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + oldCartId));
    if (!oldCart.getUserId().equals(userId)) {
      throw new ResourceNotFoundException("Cart not found: " + oldCartId);
    }
    if (oldCart.getStatus() != CartStatus.ABANDONED) {
      throw new ResourceConflictException("Only abandoned carts can be recovered");
    }

    Cart newCart = getOrCreateCart(userId);
    List<RecoverCartResponse.Warning> warnings = new java.util.ArrayList<>();
    int restored = 0;

    for (CartItem oldItem : cartItemRepository.findByCartId(oldCartId)) {
      ProductVariant variant = productVariantRepository.findById(oldItem.getProductVariantId()).orElse(null);
      if (variant == null) {
        continue;
      }
      try {
        reserveOrThrow(oldItem.getProductVariantId(), oldItem.getQuantity());
      } catch (ResourceConflictException e) {
        continue;
      }

      if (variant.getPrice().compareTo(oldItem.getPriceAtTime()) != 0) {
        warnings.add(new RecoverCartResponse.Warning(
            oldItem.getProductVariantId(), "PRICE_CHANGED", oldItem.getPriceAtTime(), variant.getPrice()));
      }

      CartItem restoredItem = new CartItem();
      restoredItem.setCartId(newCart.getId());
      restoredItem.setProductVariantId(oldItem.getProductVariantId());
      restoredItem.setQuantity(oldItem.getQuantity());
      restoredItem.setPriceAtTime(variant.getPrice());
      restoredItem.setDiscountApplied(BigDecimal.ZERO);
      restoredItem.setLineTotal(lineTotal(restoredItem));
      LocalDateTime now = LocalDateTime.now();
      restoredItem.setCreatedAt(now);
      restoredItem.setUpdatedAt(now);
      CartItem saved = cartItemRepository.save(restoredItem);

      Reservation reservation = new Reservation();
      reservation.setCartItemId(saved.getId());
      reservation.setProductVariantId(oldItem.getProductVariantId());
      reservation.setQuantity(oldItem.getQuantity());
      reservation.setCreatedAt(now);
      reservation.setExpiresAt(now.plusHours(RESERVATION_HOLD_HOURS));
      reservationRepository.save(reservation);

      restored++;
    }

    touchCart(newCart);
    return new RecoverCartResponse(newCart.getId(), "Abandoned cart recovered", restored, warnings);
  }

  private void reserveOrThrow(Long variantId, int quantity) {
    try {
      inventoryService.reserveInventory(variantId, quantity);
    } catch (OutOfStockException e) {
      int available = inventoryService.checkAvailability(variantId, quantity);
      throw new ResourceConflictException(
          "OUT_OF_STOCK",
          "Requested quantity exceeds available stock",
          Map.of("requested_quantity", quantity, "available_quantity", available));
    }
  }

  private void adjustReservation(Long cartItemId, Long variantId, int delta) {
    List<Reservation> reservations = reservationRepository.findByCartItemId(cartItemId);
    Reservation reservation = reservations.isEmpty() ? null : reservations.get(0);
    LocalDateTime now = LocalDateTime.now();
    if (reservation == null) {
      reservation = new Reservation();
      reservation.setCartItemId(cartItemId);
      reservation.setProductVariantId(variantId);
      reservation.setQuantity(0);
      reservation.setCreatedAt(now);
    }
    reservation.setQuantity(Math.max(0, reservation.getQuantity() + delta));
    reservation.setExpiresAt(now.plusHours(RESERVATION_HOLD_HOURS));
    reservationRepository.save(reservation);
  }

  private CartItem getOwnedItem(Long cartId, Long cartItemId) {
    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
    if (!item.getCartId().equals(cartId)) {
      throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
    }
    return item;
  }

  private void touchCart(Cart cart) {
    LocalDateTime now = LocalDateTime.now();
    cart.setUpdatedAt(now);
    cart.setExpiresAt(now.plusHours(RESERVATION_HOLD_HOURS));
    cartRepository.save(cart);
  }

  private BigDecimal lineTotal(CartItem item) {
    return grossLineTotal(item).subtract(item.getDiscountApplied() != null ? item.getDiscountApplied() : BigDecimal.ZERO);
  }

  private BigDecimal grossLineTotal(CartItem item) {
    return item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity()));
  }

  private int totalItemCount(Long cartId) {
    return cartItemRepository.findByCartId(cartId).stream().mapToInt(CartItem::getQuantity).sum();
  }

  CartResponse toResponse(Cart cart) {
    List<CartItemDetail> items = cartItemRepository.findByCartId(cart.getId()).stream()
        .map(this::toItemDetail)
        .toList();
    List<AppliedPromotion> promotions = cartPromotionRepository.findByCartId(cart.getId()).stream()
        .map(p -> new AppliedPromotion(p.getPromotionId(), p.getCouponCode(), p.getDiscountAmount()))
        .toList();
    return new CartResponse(
        cart.getId(),
        cart.getUserId(),
        cart.getStatus().name(),
        items,
        promotions,
        calculateTotals(cart.getId()),
        cart.getCreatedAt(),
        cart.getExpiresAt());
  }

  private CartItemDetail toItemDetail(CartItem item) {
    ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
    int available = variant != null ? inventoryService.checkAvailability(item.getProductVariantId(), item.getQuantity()) : 0;
    return new CartItemDetail(
        item.getId(),
        item.getProductVariantId(),
        variant != null ? variant.getProduct().getName() : null,
        variant != null ? variant.getSku() : null,
        variant != null ? readAttributes(variant.getVariantAttributes()) : null,
        item.getQuantity(),
        item.getPriceAtTime(),
        item.getDiscountApplied(),
        item.getLineTotal(),
        variant != null);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readAttributes(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }
}
