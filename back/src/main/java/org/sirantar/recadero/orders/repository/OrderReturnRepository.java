package org.sirantar.recadero.orders.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.orders.domain.OrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for order return/RMA requests.
 */
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

  List<OrderReturn> findByOrderId(Long orderId);

  Optional<OrderReturn> findByReturnNumber(String returnNumber);
}
