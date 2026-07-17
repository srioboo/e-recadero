package org.sirantar.recadero.orders.repository;

import java.util.List;
import org.sirantar.recadero.orders.domain.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for individual payment transactions (captures, refunds) on an order.
 */
public interface OrderTransactionRepository extends JpaRepository<OrderTransaction, Long> {

  List<OrderTransaction> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
