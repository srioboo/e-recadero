package org.sirantar.recadero.promotions.domain;

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
 * A single redemption of a promotion (via a coupon code or automatically) on an order.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion_usage", schema = "promotions")
public class PromotionUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  @Column(name = "coupon_code_id")
  private Long couponCodeId;

  @Column(name = "order_id")
  private Long orderId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "discount_applied", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountAmount;

  @Column(name = "used_at", nullable = false)
  private LocalDateTime usedAt;
}
