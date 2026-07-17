package org.sirantar.recadero.promotions.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for promotion campaigns.
 */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  @Query(
      """
      select p
      from Promotion p
      where (:status is null or p.status = :status)
        and (:type is null or p.type = :type)
        and (:fromDate is null or p.startDate >= :fromDate)
        and (:toDate is null or p.startDate <= :toDate)
      order by p.priority desc, p.createdAt desc
      """)
  Page<Promotion> search(
      @Param("status") PromotionStatus status,
      @Param("type") PromotionType type,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      Pageable pageable);

  @Query(
      """
      select p
      from Promotion p
      where p.status = 'ACTIVE'
        and p.startDate <= :now
        and (p.endDate is null or p.endDate >= :now)
      order by p.priority desc
      """)
  List<Promotion> findAllActive(@Param("now") LocalDateTime now);

  List<Promotion> findByStatusAndEndDateBefore(PromotionStatus status, LocalDateTime threshold);
}
