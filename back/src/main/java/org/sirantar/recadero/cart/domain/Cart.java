package org.sirantar.recadero.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A customer's shopping cart. Only one ACTIVE cart exists per user at a time
 * (enforced by a partial unique index — see V11 migration).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shopping_cart", schema = "cart")
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CartStatus status = CartStatus.ACTIVE;

  @Column(name = "checkout_token", length = 255)
  private String checkoutToken;

  @Column(name = "billing_address_id")
  private Long billingAddressId;

  @Column(name = "shipping_address_id")
  private Long shippingAddressId;

  @Column(name = "shipping_method_id", length = 255)
  private String shippingMethodId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Version
  private Long version;
}
