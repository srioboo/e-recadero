package org.sirantar.recadero.catalog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.sirantar.recadero.catalog.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for inventory records.
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  @Query(
      """
      select i
      from Inventory i
      where i.productVariant.id = :variantId
        and i.warehouseId = :warehouseId
      """)
  Optional<Inventory> findByProductVariantIdAndWarehouseId(
      @Param("variantId") Long variantId, @Param("warehouseId") UUID warehouseId);

  List<Inventory> findByProductVariant_Id(Long variantId);
}
