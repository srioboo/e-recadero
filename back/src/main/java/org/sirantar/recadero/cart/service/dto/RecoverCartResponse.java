package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for POST /api/v1/cart/recover/{old_cart_id}.
 */
public record RecoverCartResponse(
    @JsonProperty("cart_id") Long cartId,
    String message,
    @JsonProperty("items_restored") int itemsRestored,
    List<Warning> warnings) {

  public record Warning(
      @JsonProperty("product_variant_id") Long productVariantId,
      String issue,
      @JsonProperty("old_price") BigDecimal oldPrice,
      @JsonProperty("new_price") BigDecimal newPrice) {}
}
