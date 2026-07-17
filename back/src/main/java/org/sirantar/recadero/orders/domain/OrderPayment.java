package org.sirantar.recadero.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * The payment record for an order (payment processing itself happens
 * upstream in Cart's confirm-checkout; this is a record of the result).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_payment", schema = "orders")
public class OrderPayment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "payment_method", nullable = false, length = 100)
  private String paymentMethod;

  @Column(name = "transaction_id", length = 255)
  private String transactionId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(length = 3)
  private String currency = "USD";

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false, length = 50)
  private PaymentStatus status;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
