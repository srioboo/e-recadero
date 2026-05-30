package org.sirantar.recadero.catalog.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for category records.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByParentCategory_IdOrderBySortOrder(Long parentCategoryId);

  Optional<Category> findBySlug(String slug);
}
