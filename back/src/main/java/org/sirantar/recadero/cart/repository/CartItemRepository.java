package org.sirantar.recadero.cart.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for cart line items.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  List<CartItem> findByCartId(Long cartId);

  Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);

  void deleteByCartId(Long cartId);

  int countByCartId(Long cartId);
}
