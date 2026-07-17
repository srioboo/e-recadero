package org.sirantar.recadero.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A customer order. Addresses are stored as plain IDs (snapshots, not FKs)
 * so a later address edit/delete never affects historical orders.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`order`", schema = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal taxTotal = BigDecimal.ZERO;

  @Column(name = "shipping_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal shippingTotal = BigDecimal.ZERO;

  @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountTotal = BigDecimal.ZERO;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal grandTotal;

  @Column(name = "billing_address_id")
  private Long billingAddressId;

  @Column(name = "shipping_address_id")
  private Long shippingAddressId;

  @Column(name = "shipping_method_id", length = 255)
  private String shippingMethodId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime orderDate;

  @Column(name = "confirmed_date")
  private LocalDateTime confirmedDate;

  @Column(name = "shipped_date")
  private LocalDateTime shippedDate;

  @Column(name = "delivered_date")
  private LocalDateTime deliveredDate;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  private Long version;
}
