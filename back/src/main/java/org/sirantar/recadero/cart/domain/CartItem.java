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
 * A line item within a cart, pinned to the product variant's price at the
 * time it was added.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_item", schema = "cart")
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cart_id", nullable = false)
  private Long cartId;

  @Column(name = "variant_id", nullable = false)
  private Long productVariantId;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal priceAtTime;

  @Column(name = "discount_applied", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountApplied = BigDecimal.ZERO;

  @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
