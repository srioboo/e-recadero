package org.sirantar.recadero.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutResponse;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutRequest;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private CartPromotionRepository cartPromotionRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private CartValidationService cartValidationService;
  @Mock private CartEventPublisher eventPublisher;

  private CartService cartService;
  private CheckoutService checkoutService;
  private Cart activeCart;
  private CartItem item;

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
        new com.fasterxml.jackson.databind.ObjectMapper());

    checkoutService = new CheckoutService(
        cartRepository, cartItemRepository, cartPromotionRepository, reservationRepository,
        productVariantRepository, cartService, eventPublisher);

    activeCart = new Cart();
    activeCart.setId(1L);
    activeCart.setUserId(10L);
    activeCart.setStatus(CartStatus.ACTIVE);
    activeCart.setCreatedAt(LocalDateTime.now());
    activeCart.setUpdatedAt(LocalDateTime.now());

    item = new CartItem();
    item.setId(500L);
    item.setCartId(1L);
    item.setProductVariantId(50L);
    item.setQuantity(2);
    item.setPriceAtTime(BigDecimal.valueOf(19.99));
    item.setDiscountApplied(BigDecimal.ZERO);
    item.setLineTotal(BigDecimal.valueOf(39.98));

    org.mockito.Mockito.lenient().when(cartRepository.findByUserIdAndStatus(10L, CartStatus.ACTIVE)).thenReturn(Optional.of(activeCart));
    org.mockito.Mockito.lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    org.mockito.Mockito.lenient().when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));
    org.mockito.Mockito.lenient().when(cartPromotionRepository.findByCartId(1L)).thenReturn(List.of());
  }

  @Test
  void prepareCheckoutDetectsFlashSalePriceChange() {
    Product product = new Product();
    product.setId(100L);
    ProductVariant variant = new ProductVariant();
    variant.setId(50L);
    variant.setProduct(product);
    variant.setPrice(BigDecimal.valueOf(14.99));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant));

    assertThatThrownBy(() -> checkoutService.prepareCheckout(10L, new PrepareCheckoutRequest(null, null, null)))
        .isInstanceOf(ResourceConflictException.class)
        .satisfies(ex -> assertThat(((ResourceConflictException) ex).getErrorCode()).isEqualTo("CART_MODIFIED"));
  }

  @Test
  void prepareCheckoutLocksCartWhenPricesUnchanged() {
    ProductVariant variant = new ProductVariant();
    variant.setId(50L);
    variant.setPrice(BigDecimal.valueOf(19.99));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant));

    var response = checkoutService.prepareCheckout(10L, new PrepareCheckoutRequest(null, null, null));

    assertThat(response.status()).isEqualTo("LOCKED_FOR_CHECKOUT");
    assertThat(response.checkoutToken()).isNotBlank();
    assertThat(activeCart.getStatus()).isEqualTo(CartStatus.LOCKED_FOR_CHECKOUT);
  }

  @Test
  void confirmCheckoutRejectsMismatchedToken() {
    activeCart.setStatus(CartStatus.LOCKED_FOR_CHECKOUT);
    activeCart.setCheckoutToken("real-token");
    activeCart.setExpiresAt(LocalDateTime.now().plusMinutes(10));

    assertThatThrownBy(() -> checkoutService.confirmCheckout(
        10L, new ConfirmCheckoutRequest("wrong-token", "CREDIT_CARD", "txn-1")))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void confirmCheckoutClearsCartAndPublishesEventWhenTokenMatches() {
    activeCart.setStatus(CartStatus.LOCKED_FOR_CHECKOUT);
    activeCart.setCheckoutToken("real-token");
    activeCart.setExpiresAt(LocalDateTime.now().plusMinutes(10));

    ConfirmCheckoutResponse response = checkoutService.confirmCheckout(
        10L, new ConfirmCheckoutRequest("real-token", "CREDIT_CARD", "txn-1"));

    assertThat(activeCart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
    assertThat(response.cartId()).isEqualTo(1L);
    assertThat(response.orderId()).isNull();
    org.mockito.Mockito.verify(eventPublisher).publishCheckoutCompleted(
        org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(10L),
        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("CREDIT_CARD"),
        org.mockito.ArgumentMatchers.eq("txn-1"), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("real-token"),
        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }
}
