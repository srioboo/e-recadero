package org.sirantar.recadero.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.sirantar.recadero.cart.service.CartPromotionService;
import org.sirantar.recadero.cart.service.CartService;
import org.sirantar.recadero.cart.service.CartValidationService;
import org.sirantar.recadero.cart.service.CheckoutService;
import org.sirantar.recadero.cart.service.CouponValidator;
import org.sirantar.recadero.cart.service.dto.AddItemRequest;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutResponse;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutResponse;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;

/**
 * End-to-end workflow test for the Cart module: add product → adjust
 * quantity → apply coupon → prepare checkout → confirm checkout, plus
 * cart expiration/recovery, exercising the service layer with mocked
 * persistence (mirrors the other modules' *IntegrationTest style).
 */
@DisplayName("Cart Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class CartIntegrationTest {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private CartPromotionRepository cartPromotionRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private InventoryService inventoryService;
  @Mock private CartValidationService cartValidationService;
  @Mock private CartEventPublisher eventPublisher;

  private CartService cartService;
  private CartPromotionService cartPromotionService;
  private CheckoutService checkoutService;

  private Cart cart;
  private ProductVariant variant;
  private final List<CartItem> storedItems = new ArrayList<>();
  private final List<CartPromotion> storedPromotions = new ArrayList<>();

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    cartService = new CartService(
        cartRepository, cartItemRepository, cartPromotionRepository, reservationRepository,
        productVariantRepository, inventoryService, cartValidationService, eventPublisher, objectMapper);
    CouponValidator acceptingValidator = (code, subtotal) -> new CouponValidator.CouponValidationResult(
        true, null, 1L, "PERCENTAGE", BigDecimal.TEN, subtotal.multiply(BigDecimal.valueOf(0.10)), true, true, true);
    cartPromotionService = new CartPromotionService(cartPromotionRepository, cartService, acceptingValidator);
    checkoutService = new CheckoutService(
        cartRepository, cartItemRepository, cartPromotionRepository, reservationRepository,
        productVariantRepository, cartService, eventPublisher);

    cart = new Cart();
    cart.setId(1L);
    cart.setUserId(10L);
    cart.setStatus(CartStatus.ACTIVE);
    cart.setCreatedAt(LocalDateTime.now());
    cart.setUpdatedAt(LocalDateTime.now());

    Product product = new Product();
    product.setId(100L);
    product.setName("Widget");
    variant = new ProductVariant();
    variant.setId(50L);
    variant.setProduct(product);
    variant.setSku("WIDGET-1");
    variant.setPrice(BigDecimal.valueOf(20.00));

    var lenient = org.mockito.Mockito.lenient();
    lenient.when(cartRepository.findByUserIdAndStatus(10L, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
    lenient.when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    lenient.when(cartValidationService.requireVariant(50L)).thenReturn(variant);
    lenient.when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant));

    lenient.when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
      CartItem item = inv.getArgument(0);
      if (item.getId() == null) {
        item.setId(500L);
        storedItems.add(item);
      }
      return item;
    });
    lenient.when(cartItemRepository.findByCartId(1L)).thenAnswer(inv -> new ArrayList<>(storedItems));
    lenient.when(cartItemRepository.findByCartIdAndProductVariantId(1L, 50L))
        .thenAnswer(inv -> storedItems.stream().findFirst());
    lenient.when(cartItemRepository.findById(500L)).thenAnswer(inv -> storedItems.stream().findFirst());
    lenient.when(reservationRepository.findByCartItemId(500L)).thenReturn(List.of());

    lenient.when(cartPromotionRepository.save(any(CartPromotion.class))).thenAnswer(inv -> {
      CartPromotion promo = inv.getArgument(0);
      storedPromotions.add(promo);
      return promo;
    });
    lenient.when(cartPromotionRepository.findByCartId(1L)).thenAnswer(inv -> new ArrayList<>(storedPromotions));
  }

  @Test
  @DisplayName("Should add product, adjust quantity, apply coupon, prepare and confirm checkout")
  void addQuantityCouponPrepareConfirmCheckoutFlow() {
    var added = cartService.addItem(10L, new AddItemRequest(50L, 1));
    assertThat(added.quantity()).isEqualTo(1);

    var updated = cartService.updateItemQuantity(10L, 500L, 3);
    assertThat(updated.quantity()).isEqualTo(3);
    assertThat(updated.lineTotal()).isEqualByComparingTo("60.00");

    var coupon = cartPromotionService.applyCoupon(10L, "WELCOME10");
    assertThat(coupon.discountAmount()).isEqualByComparingTo("6.00");

    PrepareCheckoutResponse prepared = checkoutService.prepareCheckout(10L, new PrepareCheckoutRequest(null, null, null));
    assertThat(prepared.status()).isEqualTo("LOCKED_FOR_CHECKOUT");
    assertThat(prepared.calculations().grandTotal()).isEqualByComparingTo("54.00");

    ConfirmCheckoutResponse confirmed = checkoutService.confirmCheckout(
        10L, new ConfirmCheckoutRequest(prepared.checkoutToken(), "CREDIT_CARD", "txn-123"));
    assertThat(confirmed.cartId()).isEqualTo(1L);
    assertThat(cart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
  }

  @Test
  @DisplayName("Should mark an inactive cart abandoned and allow recovery")
  void expirationThenRecoveryFlow() {
    Cart abandoned = new Cart();
    abandoned.setId(2L);
    abandoned.setUserId(10L);
    abandoned.setStatus(CartStatus.ABANDONED);
    abandoned.setCreatedAt(LocalDateTime.now().minusDays(2));
    abandoned.setUpdatedAt(LocalDateTime.now().minusDays(1));

    CartItem oldItem = new CartItem();
    oldItem.setId(600L);
    oldItem.setCartId(2L);
    oldItem.setProductVariantId(50L);
    oldItem.setQuantity(2);
    oldItem.setPriceAtTime(BigDecimal.valueOf(18.00));
    oldItem.setDiscountApplied(BigDecimal.ZERO);
    oldItem.setLineTotal(BigDecimal.valueOf(36.00));

    when(cartRepository.findById(2L)).thenReturn(Optional.of(abandoned));
    when(cartItemRepository.findByCartId(2L)).thenReturn(List.of(oldItem));

    var response = cartService.recoverCart(10L, 2L);

    assertThat(response.itemsRestored()).isEqualTo(1);
    assertThat(response.warnings()).hasSize(1);
    assertThat(response.warnings().get(0).issue()).isEqualTo("PRICE_CHANGED");
  }
}
