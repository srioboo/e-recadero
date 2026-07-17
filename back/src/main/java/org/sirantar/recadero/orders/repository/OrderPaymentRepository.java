package org.sirantar.recadero.orders.repository;

import java.util.Optional;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for order payment records.
 */
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

  Optional<OrderPayment> findByTransactionId(String transactionId);

  Optional<OrderPayment> findByOrderId(Long orderId);
}
