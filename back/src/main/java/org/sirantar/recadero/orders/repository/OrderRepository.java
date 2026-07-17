package org.sirantar.recadero.orders.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByOrderNumber(String orderNumber);

  Page<Order> findByUserId(Long userId, Pageable pageable);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  @Query(
      """
      select o
      from Order o
      where (:userId is null or o.userId = :userId)
        and (:status is null or o.status = :status)
        and (:fromDate is null or o.orderDate >= :fromDate)
        and (:toDate is null or o.orderDate <= :toDate)
        and (:minTotal is null or o.grandTotal >= :minTotal)
        and (:maxTotal is null or o.grandTotal <= :maxTotal)
      """)
  Page<Order> search(
      @Param("userId") Long userId,
      @Param("status") OrderStatus status,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      @Param("minTotal") java.math.BigDecimal minTotal,
      @Param("maxTotal") java.math.BigDecimal maxTotal,
      Pageable pageable);
}
