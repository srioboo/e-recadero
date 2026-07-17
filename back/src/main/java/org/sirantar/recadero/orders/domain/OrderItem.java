package org.sirantar.recadero.orders.domain;

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
 * An immutable snapshot of a purchased line item (product name/SKU/price
 * captured at order time, independent of later catalog changes).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_item", schema = "orders")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "variant_id", nullable = false)
  private Long productVariantId;

  @Column(name = "sku", length = 100)
  private String productSku;

  @Column(name = "product_name", length = 255)
  private String productName;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_discount", nullable = false, precision = 10, scale = 2)
  private BigDecimal lineDiscount = BigDecimal.ZERO;

  @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
