package org.sirantar.recadero.promotions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A redeemable code for a promotion.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coupon_code", schema = "promotions")
public class CouponCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String code;

  @Column(name = "promotion_id")
  private Long promotionId;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "usage_count", nullable = false)
  private Integer currentUsage = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at")
  private LocalDateTime expiryDate;

  @Column(name = "created_by", length = 255)
  private String createdBy;
}
