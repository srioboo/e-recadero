package org.sirantar.recadero.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductStatus;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.service.dto.CategoryCreateRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryMoveRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryResponse;
import org.sirantar.recadero.catalog.service.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Catalog category management.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CatalogService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final CategoryValidationService categoryValidationService;

  public CategoryResponse createCategory(CategoryCreateRequest request) {
    categoryValidationService.validateForCreate(request.slug(), request.parentCategoryId());
    Category category = new Category();
    applyRequest(category, request.name(), request.slug(), request.description(), request.imageUrl(),
        request.sortOrder(), request.active(), request.parentCategoryId());
    return toResponse(categoryRepository.save(category));
  }

  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    categoryValidationService.validateForUpdate(id, request.slug(), request.parentCategoryId());
    applyRequest(category, request.name(), request.slug(), request.description(), request.imageUrl(),
        request.sortOrder(), request.active(), request.parentCategoryId());
    return toResponse(categoryRepository.save(category));
  }

  @Transactional(readOnly = true)
  public Page<CategoryResponse> listCategories(Pageable pageable) {
    return categoryRepository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public CategoryResponse getCategoryWithChildren(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    return toResponseWithChildren(category);
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getCategoryChildren(Long id) {
    return categoryRepository.findByParentCategory_IdOrderBySortOrder(id).stream()
        .map(this::toResponse)
        .toList();
  }

  public void deleteCategoryLogical(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

    archiveCategoryProducts(category.getId());
    category.setActive(Boolean.FALSE);
    categoryRepository.save(category);
  }

  public CategoryResponse moveCategory(Long id, CategoryMoveRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    categoryValidationService.validateForUpdate(id, category.getSlug(), request.parentCategoryId());
    category.setParentCategory(resolveParent(id, request.parentCategoryId()));
    return toResponse(categoryRepository.save(category));
  }

  private void applyRequest(
      Category category,
      String name,
      String slug,
      String description,
      String imageUrl,
      Integer sortOrder,
      Boolean active,
      Long parentCategoryId) {
    if (name != null) {
      category.setName(name);
    }
    if (slug != null) {
      category.setSlug(slug);
    }
    if (description != null) {
      category.setDescription(description);
    }
    if (imageUrl != null) {
      category.setImageUrl(imageUrl);
    }
    if (sortOrder != null) {
      category.setSortOrder(sortOrder);
    }
    if (active != null) {
      category.setActive(active);
    } else if (category.getId() == null) {
      category.setActive(Boolean.TRUE);
    }
    category.setParentCategory(resolveParent(category.getId(), parentCategoryId));

  }

  private Category resolveParent(Long categoryId, Long parentCategoryId) {
    if (parentCategoryId == null) {
      return null;
    }
    Category parent = categoryRepository.findById(parentCategoryId)
        .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + parentCategoryId));
    if (categoryId != null && categoryId.equals(parent.getId())) {
      throw new IllegalArgumentException("Category cannot be its own parent");
    }
    return parent;
  }

  private void archiveCategoryProducts(Long categoryId) {
    for (ProductStatus status : ProductStatus.values()) {
      List<Product> products = new ArrayList<>(productRepository.findByCategoryIdAndStatus(categoryId, status));
      for (Product product : products) {
        product.setStatus(ProductStatus.ARCHIVED);
      }
      productRepository.saveAll(products);
    }
  }

  private CategoryResponse toResponse(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getSlug(),
        category.getDescription(),
        category.getParentCategory() != null ? category.getParentCategory().getId() : null,
        category.getImageUrl(),
        category.getActive(),
        category.getSortOrder(),
        List.of());
  }

  private CategoryResponse toResponseWithChildren(Category category) {
    List<CategoryResponse> children = categoryRepository.findByParentCategory_IdOrderBySortOrder(category.getId())
        .stream()
        .map(this::toResponseWithChildren)
        .toList();
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getSlug(),
        category.getDescription(),
        category.getParentCategory() != null ? category.getParentCategory().getId() : null,
        category.getImageUrl(),
        category.getActive(),
        category.getSortOrder(),
        children);
  }
}
