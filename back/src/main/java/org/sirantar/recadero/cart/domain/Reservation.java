package org.sirantar.recadero.cart.domain;

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
 * An inventory reservation held against a cart item until checkout or expiry.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservation", schema = "cart")
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cart_item_id", nullable = false)
  private Long cartItemId;

  @Column(name = "product_variant_id", nullable = false)
  private Long productVariantId;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;
}
