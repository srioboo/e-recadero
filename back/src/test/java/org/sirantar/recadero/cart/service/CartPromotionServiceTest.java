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
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.dto.ApplyCouponResponse;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class CartPromotionServiceTest {

  @Mock private CartPromotionRepository cartPromotionRepository;
  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;

  private CartService cartService;
  private Cart activeCart;

  @BeforeEach
  void setUp() {
    cartService = new CartService(
        cartRepository,
        cartItemRepository,
        cartPromotionRepository,
        org.mockito.Mockito.mock(ReservationRepository.class),
        org.mockito.Mockito.mock(ProductVariantRepository.class),
        org.mockito.Mockito.mock(InventoryService.class),
        org.mockito.Mockito.mock(CartValidationService.class),
        org.mockito.Mockito.mock(org.sirantar.recadero.cart.events.CartEventPublisher.class),
        new com.fasterxml.jackson.databind.ObjectMapper());

    activeCart = new Cart();
    activeCart.setId(1L);
    activeCart.setUserId(10L);
    activeCart.setStatus(CartStatus.ACTIVE);
    activeCart.setCreatedAt(LocalDateTime.now());
    activeCart.setUpdatedAt(LocalDateTime.now());

    when(cartRepository.findByUserIdAndStatus(10L, CartStatus.ACTIVE)).thenReturn(Optional.of(activeCart));
    when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());
  }

  @Test
  void applyCouponRejectsUnknownCode() {
    CouponValidator alwaysInvalid = (code, subtotal, variantIds, userId) ->
        CouponValidator.CouponValidationResult.invalid("Coupon code " + code + " not found or expired");
    CartPromotionService service = new CartPromotionService(cartPromotionRepository, cartService, alwaysInvalid);

    assertThatThrownBy(() -> service.applyCoupon(10L, "MISSING10"))
        .isInstanceOf(BusinessLogicException.class)
        .satisfies(ex -> assertThat(((BusinessLogicException) ex).getErrorCode()).isEqualTo("INVALID_COUPON"));
  }

  @Test
  void applyCouponPersistsPromotionWhenValidatorAccepts() {
    CouponValidator validator = (code, subtotal, variantIds, userId) -> new CouponValidator.CouponValidationResult(
        true, null, 7L, "PERCENTAGE", BigDecimal.TEN, BigDecimal.valueOf(15), true, true, true);
    CartPromotionService service = new CartPromotionService(cartPromotionRepository, cartService, validator);
    when(cartPromotionRepository.save(any(CartPromotion.class))).thenAnswer(inv -> inv.getArgument(0));

    ApplyCouponResponse response = service.applyCoupon(10L, "WELCOME10");

    assertThat(response.couponCode()).isEqualTo("WELCOME10");
    assertThat(response.discountAmount()).isEqualByComparingTo("15");
  }

  @Test
  void applyCouponRejectsWhenMinimumOrderNotMet() {
    CouponValidator validator = (code, subtotal, variantIds, userId) -> new CouponValidator.CouponValidationResult(
        false, "Coupon HIGHMINIMUM requires minimum order of $500", null, null, null, null, false, true, true);
    CartPromotionService service = new CartPromotionService(cartPromotionRepository, cartService, validator);

    assertThatThrownBy(() -> service.applyCoupon(10L, "HIGHMINIMUM"))
        .isInstanceOf(ResourceConflictException.class)
        .satisfies(ex -> assertThat(((ResourceConflictException) ex).getErrorCode()).isEqualTo("COUPON_NOT_APPLICABLE"));
  }
}
