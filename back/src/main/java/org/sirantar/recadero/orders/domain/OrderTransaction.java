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
 * A single payment transaction (capture, refund, ...) against an order,
 * forming the refund history for {@code GET .../payment}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_transaction", schema = "orders")
public class OrderTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "payment_id")
  private Long paymentId;

  @Column(name = "transaction_type", nullable = false, length = 50)
  private String transactionType;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "response_message", length = 500)
  private String responseMessage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
