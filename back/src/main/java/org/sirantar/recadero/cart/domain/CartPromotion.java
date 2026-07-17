package org.sirantar.recadero.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A coupon/promotion applied to a cart (Promotions module — Phase 7 — is
 * not yet implemented; see CartPromotionService's CouponValidator port).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_promotion", schema = "cart")
public class CartPromotion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cart_id", nullable = false)
  private Long cartId;

  @Column(name = "promotion_id")
  private Long promotionId;

  @Column(name = "coupon_code", length = 100)
  private String couponCode;

  @Column(name = "discount_type", length = 50)
  private String discountType;

  @Column(name = "discount_value", precision = 10, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "applied_at", nullable = false, updatable = false)
  private LocalDateTime appliedAt;

  @Column(name = "applied_by", length = 255)
  private String appliedBy;
}
