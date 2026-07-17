package org.sirantar.recadero.orders.domain;

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
 * A single tracking update recorded against a shipment (carrier webhook history).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_shipment_event", schema = "orders")
public class OrderShipmentEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shipment_id", nullable = false)
  private Long shipmentId;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(length = 255)
  private String location;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;
}
