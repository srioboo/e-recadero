package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload for POST /api/v1/coupons/validate (called by Cart module).
 */
public record ValidateCouponRequest(
    @JsonProperty("coupon_code") String couponCode,
    @JsonProperty("cart_items") List<CartItemPayload> cartItems,
    BigDecimal subtotal,
    @JsonProperty("user_id") Long userId) {

  public record CartItemPayload(
      @JsonProperty("product_id") Long productId,
      @JsonProperty("product_variant_id") Long productVariantId,
      @JsonProperty("category_id") Long categoryId,
      int quantity,
      BigDecimal price) {}
}
