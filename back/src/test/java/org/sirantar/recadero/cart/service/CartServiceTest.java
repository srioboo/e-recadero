package org.sirantar.recadero.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartItem;
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.dto.AddItemRequest;
import org.sirantar.recadero.cart.service.dto.AddItemResponse;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.catalog.service.OutOfStockException;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private CartPromotionRepository cartPromotionRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private CartValidationService cartValidationService;
  @Mock private CartEventPublisher eventPublisher;

  private CartService cartService;
  private Cart activeCart;
  private ProductVariant variant;

  @BeforeEach
  void setUp() {
    cartService = new CartService(
        cartRepository,
        cartItemRepository,
        cartPromotionRepository,
        reservationRepository,
        productVariantRepository,
        inventoryService,
        cartValidationService,
        eventPublisher,
        new ObjectMapper());

    activeCart = new Cart();
    activeCart.setId(1L);
    activeCart.setUserId(10L);
    activeCart.setStatus(CartStatus.ACTIVE);
    activeCart.setCreatedAt(LocalDateTime.now());
    activeCart.setUpdatedAt(LocalDateTime.now());
    activeCart.setExpiresAt(LocalDateTime.now().plusHours(24));

    Product product = new Product();
    product.setId(100L);
    product.setName("Widget");

    variant = new ProductVariant();
    variant.setId(50L);
    variant.setProduct(product);
    variant.setSku("WIDGET-1");
    variant.setPrice(BigDecimal.valueOf(19.99));

    lenient().when(cartRepository.findByUserIdAndStatus(10L, CartStatus.ACTIVE)).thenReturn(Optional.of(activeCart));
    lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void addItemReservesInventoryAndCreatesLineItem() {
    when(cartValidationService.requireVariant(50L)).thenReturn(variant);
    when(cartItemRepository.findByCartIdAndProductVariantId(1L, 50L)).thenReturn(Optional.empty());
    when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
      CartItem item = inv.getArgument(0);
      if (item.getId() == null) item.setId(500L);
      return item;
    });
    when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

    AddItemResponse response = cartService.addItem(10L, new AddItemRequest(50L, 2));

    assertThat(response.productVariantId()).isEqualTo(50L);
    assertThat(response.quantity()).isEqualTo(2);
    assertThat(response.lineTotal()).isEqualByComparingTo("39.98");
    assertThat(response.message()).isEqualTo("Item added to cart");
  }

  @Test
  void addItemTranslatesOutOfStockToResourceConflict() {
    when(cartValidationService.requireVariant(50L)).thenReturn(variant);
    when(cartItemRepository.findByCartIdAndProductVariantId(1L, 50L)).thenReturn(Optional.empty());
    org.mockito.Mockito.doThrow(new OutOfStockException("insufficient"))
        .when(inventoryService).reserveInventory(50L, 10);
    when(inventoryService.checkAvailability(50L, 10)).thenReturn(3);

    assertThatThrownBy(() -> cartService.addItem(10L, new AddItemRequest(50L, 10)))
        .isInstanceOf(ResourceConflictException.class)
        .satisfies(ex -> assertThat(((ResourceConflictException) ex).getErrorCode()).isEqualTo("OUT_OF_STOCK"));
  }

  @Test
  void updateItemQuantityReservesAdditionalStockOnIncrease() {
    CartItem item = existingItem();
    when(cartItemRepository.findById(500L)).thenReturn(Optional.of(item));
    when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
    when(reservationRepository.findByCartItemId(500L)).thenReturn(List.of());
    when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

    cartService.updateItemQuantity(10L, 500L, 5);

    org.mockito.Mockito.verify(inventoryService).reserveInventory(50L, 3);
  }

  @Test
  void updateItemQuantityReleasesStockOnDecrease() {
    CartItem item = existingItem();
    when(cartItemRepository.findById(500L)).thenReturn(Optional.of(item));
    when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
    when(reservationRepository.findByCartItemId(500L)).thenReturn(List.of());
    when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

    cartService.updateItemQuantity(10L, 500L, 1);

    org.mockito.Mockito.verify(inventoryService).releaseReservation(50L, 1);
  }

  @Test
  void calculateTotalsSubtractsDiscountFromSubtotal() {
    CartItem item = existingItem();
    when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

    CartPromotion promo = new CartPromotion();
    promo.setDiscountAmount(BigDecimal.valueOf(10));
    when(cartPromotionRepository.findByCartId(1L)).thenReturn(List.of(promo));

    var totals = cartService.calculateTotals(1L);

    assertThat(totals.subtotal()).isEqualByComparingTo("39.98");
    assertThat(totals.discountTotal()).isEqualByComparingTo("10");
    assertThat(totals.grandTotal()).isEqualByComparingTo("29.98");
  }

  private CartItem existingItem() {
    CartItem item = new CartItem();
    item.setId(500L);
    item.setCartId(1L);
    item.setProductVariantId(50L);
    item.setQuantity(2);
    item.setPriceAtTime(BigDecimal.valueOf(19.99));
    item.setDiscountApplied(BigDecimal.ZERO);
    item.setLineTotal(BigDecimal.valueOf(39.98));
    item.setCreatedAt(LocalDateTime.now());
    item.setUpdatedAt(LocalDateTime.now());
    return item;
  }
}
