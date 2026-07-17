package org.sirantar.recadero.orders.repository;

import java.util.List;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for order line items.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  List<OrderItem> findByOrderId(Long orderId);

  int countByOrderId(Long orderId);
}
