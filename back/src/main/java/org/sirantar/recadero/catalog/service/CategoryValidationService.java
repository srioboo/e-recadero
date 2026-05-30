package org.sirantar.recadero.catalog.service;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.springframework.stereotype.Service;

/**
 * Category validation rules for hierarchy and slug constraints.
 */
@Service
@RequiredArgsConstructor
public class CategoryValidationService {

  private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");
  private static final int MAX_DEPTH = 5;

  private final CategoryRepository categoryRepository;

  public void validateForCreate(String slug, Long parentCategoryId) {
    validateSlug(slug);
    validateUniqueSlug(slug, null);
    validateParentExists(parentCategoryId);
    validateDepth(parentCategoryId);
  }

  public void validateForUpdate(Long categoryId, String slug, Long parentCategoryId) {
    validateSlug(slug);
    validateUniqueSlug(slug, categoryId);
    validateParentExists(parentCategoryId);
    validateNoCycle(categoryId, parentCategoryId);
    validateDepth(parentCategoryId);
  }

  private void validateSlug(String slug) {
    if (slug == null || slug.isBlank() || !SLUG_PATTERN.matcher(slug).matches()) {
      throw new IllegalArgumentException("Slug must be URL-safe and non-empty");
    }
  }

  private void validateUniqueSlug(String slug, Long currentCategoryId) {
    categoryRepository.findBySlug(slug).ifPresent(existing -> {
      if (currentCategoryId == null || !existing.getId().equals(currentCategoryId)) {
        throw new IllegalArgumentException("Slug must be unique");
      }
    });
  }

  private void validateParentExists(Long parentCategoryId) {
    if (parentCategoryId != null && categoryRepository.findById(parentCategoryId).isEmpty()) {
      throw new IllegalArgumentException("Parent category must exist");
    }
  }

  private void validateNoCycle(Long categoryId, Long parentCategoryId) {
    if (categoryId == null || parentCategoryId == null) {
      return;
    }
    if (categoryId.equals(parentCategoryId)) {
      throw new IllegalArgumentException("Category cannot be its own parent");
    }
    Category current = categoryRepository.findById(parentCategoryId).orElse(null);
    int depth = 0;
    while (current != null) {
      if (categoryId.equals(current.getId())) {
        throw new IllegalArgumentException("Category parent creates a cycle");
      }
      current = current.getParentCategory();
      depth++;
      if (depth > MAX_DEPTH) {
        throw new IllegalArgumentException("Category hierarchy depth must be <= 5");
      }
    }
  }

  private void validateDepth(Long parentCategoryId) {
    if (parentCategoryId == null) {
      return;
    }
    Category current = categoryRepository.findById(parentCategoryId).orElse(null);
    int depth = 1;
    while (current != null) {
      current = current.getParentCategory();
      depth++;
      if (depth > MAX_DEPTH) {
        throw new IllegalArgumentException("Category hierarchy depth must be <= 5");
      }
    }
  }
}
