package org.sirantar.recadero.cart.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.sirantar.recadero.cart.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for inventory reservations held against cart items.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @Query("select r from Reservation r where r.expiresAt < :threshold")
  List<Reservation> findExpiredReservations(@Param("threshold") LocalDateTime threshold);

  List<Reservation> findByProductVariantId(Long productVariantId);

  List<Reservation> findByCartItemId(Long cartItemId);

  void deleteByCartItemId(Long cartItemId);
}
