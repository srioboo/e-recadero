package org.sirantar.recadero.promotions.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for coupon codes.
 */
public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {

  @Query("select c from CouponCode c where upper(c.code) = upper(:code)")
  Optional<CouponCode> findByCode(@Param("code") String code);

  List<CouponCode> findByExpiryDateBefore(LocalDateTime threshold);

  Page<CouponCode> findByPromotionId(Long promotionId, Pageable pageable);

  @Query(
      """
      select c
      from CouponCode c
      where c.promotionId = :promotionId
        and (:isActive is null or c.isActive = :isActive)
      """)
  Page<CouponCode> findByPromotionIdAndActive(
      @Param("promotionId") Long promotionId, @Param("isActive") Boolean isActive, Pageable pageable);
}
