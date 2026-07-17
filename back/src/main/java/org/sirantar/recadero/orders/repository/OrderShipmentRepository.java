package org.sirantar.recadero.orders.repository;

import java.util.Optional;
import org.sirantar.recadero.orders.domain.OrderShipment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for order shipments.
 */
public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {

  Optional<OrderShipment> findByTrackingNumber(String trackingNumber);

  Optional<OrderShipment> findByOrderId(Long orderId);
}
