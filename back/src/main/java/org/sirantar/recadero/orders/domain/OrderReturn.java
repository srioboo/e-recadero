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
 * A return/RMA request for one item of an order.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_return", schema = "orders")
public class OrderReturn {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "order_item_id")
  private Long orderItemId;

  @Column(name = "return_number", nullable = false, unique = true, length = 50)
  private String returnNumber;

  @Column(length = 255)
  private String reason;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ReturnStatus status = ReturnStatus.PENDING_APPROVAL;

  @Column(name = "refund_amount", precision = 12, scale = 2)
  private BigDecimal refundAmount;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;
}
