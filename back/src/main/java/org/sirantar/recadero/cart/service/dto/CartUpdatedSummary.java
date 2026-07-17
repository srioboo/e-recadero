package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Abbreviated cart totals echoed back after a mutating cart operation.
 * {@code total_items} is populated for item add/update; {@code discount_total}
 * for coupon apply/remove — both share {@code grand_total}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartUpdatedSummary(
    @JsonProperty("total_items") Integer totalItems,
    @JsonProperty("discount_total") java.math.BigDecimal discountTotal,
    @JsonProperty("grand_total") BigDecimal grandTotal) {

  public static CartUpdatedSummary forItemChange(int totalItems, BigDecimal grandTotal) {
    return new CartUpdatedSummary(totalItems, null, grandTotal);
  }

  public static CartUpdatedSummary forDiscountChange(BigDecimal discountTotal, BigDecimal grandTotal) {
    return new CartUpdatedSummary(null, discountTotal, grandTotal);
  }
}
