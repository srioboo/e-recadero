package org.sirantar.recadero.catalog.repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for product records.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      """
      select p
      from Product p
      where p.category.id = :categoryId
        and p.status = :status
      order by p.createdAt desc
      """)
  List<Product> findByCategoryIdAndStatus(
      @Param("categoryId") Long categoryId, @Param("status") ProductStatus status);

  Optional<Product> findBySku(String sku);

  List<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status);

  @Query(
      value =
          """
          select p
          from Product p
          where (:categoryId is null or p.category.id = :categoryId)
            and (
              :query is null
              or :query = ''
              or lower(p.name) like lower(concat('%', :query, '%'))
              or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
              or lower(coalesce(p.shortDescription, '')) like lower(concat('%', :query, '%'))
              or lower(p.sku) like lower(concat('%', :query, '%'))
              or lower(p.category.name) like lower(concat('%', :query, '%'))
            )
          """,
      countQuery =
          """
          select count(p)
          from Product p
          where (:categoryId is null or p.category.id = :categoryId)
            and (
              :query is null
              or :query = ''
              or lower(p.name) like lower(concat('%', :query, '%'))
              or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
              or lower(coalesce(p.shortDescription, '')) like lower(concat('%', :query, '%'))
              or lower(p.sku) like lower(concat('%', :query, '%'))
              or lower(p.category.name) like lower(concat('%', :query, '%'))
            )
          """)
  Page<Product> search(
      @Param("query") String query, @Param("categoryId") Long categoryId, Pageable pageable);

  @Query(
      """
      select p
      from Product p
      where (:query is null or :query = ''
          or lower(p.name) like lower(concat('%', :query, '%'))
          or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
          or lower(coalesce(p.shortDescription, '')) like lower(concat('%', :query, '%'))
          or lower(p.sku) like lower(concat('%', :query, '%'))
          or lower(p.category.name) like lower(concat('%', :query, '%')))
        and (:categoryId is null or p.category.id = :categoryId)
        and (:minPrice is null or p.price >= :minPrice)
        and (:maxPrice is null or p.price <= :maxPrice)
      """)
  Page<Product> search(
      @Param("query") String query,
      @Param("categoryId") Long categoryId,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable);
}
