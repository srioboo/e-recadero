package org.sirantar.recadero.orders.repository;

import java.util.List;
import org.sirantar.recadero.orders.domain.OrderShipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for shipment tracking history entries.
 */
public interface OrderShipmentEventRepository extends JpaRepository<OrderShipmentEvent, Long> {

  List<OrderShipmentEvent> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);
}
