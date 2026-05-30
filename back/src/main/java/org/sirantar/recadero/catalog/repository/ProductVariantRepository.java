package org.sirantar.recadero.catalog.repository;

import java.util.List;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for product variant records.
 */
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

  @Query(
      """
      select pv
      from ProductVariant pv
      where pv.product.id = :productId
      order by pv.sku asc
      """)
  List<ProductVariant> findByProductId(@Param("productId") Long productId);

  @Query(
      """
      select pv
      from ProductVariant pv
      where pv.sku = :sku
      order by pv.price asc
      """)
  List<ProductVariant> findBySkuOrderByPrice(@Param("sku") String sku);
}
