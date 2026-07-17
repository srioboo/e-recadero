package org.sirantar.recadero.cart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for shopping carts.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

  Page<Cart> findByUserIdAndStatusIn(Long userId, List<CartStatus> statuses, Pageable pageable);

  List<Cart> findByStatusAndUpdatedAtBefore(CartStatus status, LocalDateTime threshold);
}
