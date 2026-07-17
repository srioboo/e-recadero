package org.sirantar.recadero.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shipment tracking information for an order.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_shipment", schema = "orders")
public class OrderShipment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "carrier_name", length = 100)
  private String carrier;

  @Column(name = "tracking_number", unique = true, length = 100)
  private String trackingNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ShipmentStatus status = ShipmentStatus.PENDING;

  @Column(name = "shipped_at")
  private LocalDateTime shippedAt;

  @Column(name = "estimated_delivery_date")
  private java.time.LocalDate estimatedDeliveryDate;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
