package org.sirantar.recadero.cart.repository;

import java.util.List;
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for coupons/promotions applied to a cart.
 */
public interface CartPromotionRepository extends JpaRepository<CartPromotion, Long> {

  List<CartPromotion> findByCartId(Long cartId);

  void deleteByCartId(Long cartId);

  void deleteByCartIdAndCouponCode(Long cartId, String couponCode);
}
