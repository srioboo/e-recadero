package org.sirantar.recadero.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.service.dto.CategoryCreateRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryResponse;
import org.sirantar.recadero.catalog.service.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private ProductRepository productRepository;
  @Mock private CategoryValidationService categoryValidationService;

  @InjectMocks private CatalogService catalogService;

  private Category testCategory;

  @BeforeEach
  void setUp() {
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Electronics");
    testCategory.setSlug("electronics");
    testCategory.setActive(true);
  }

  @Test
  void testCreateCategorySuccess() {
    CategoryCreateRequest request =
        new CategoryCreateRequest("Electronics", "electronics", "Electronic products", null, null, 1, true);

    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    CategoryResponse response = catalogService.createCategory(request);

    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Electronics");
  }

  @Test
  void testUpdateCategorySuccess() {
    CategoryUpdateRequest request =
        new CategoryUpdateRequest("Electronics Updated", "electronics", "Description", null, null, 1, true);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    CategoryResponse response = catalogService.updateCategory(1L, request);

    assertThat(response).isNotNull();
  }

  @Test
  void testUpdateCategoryNotFound() {
    CategoryUpdateRequest request =
        new CategoryUpdateRequest("Updated", "updated", "Desc", null, null, 1, true);

    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> catalogService.updateCategory(999L, request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testListCategories() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Category> categoryPage = new PageImpl<>(List.of(testCategory), pageable, 1);

    when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

    Page<CategoryResponse> result = catalogService.listCategories(pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void testGetCategoryWithChildren() {
    Category child = new Category();
    child.setId(2L);
    child.setName("Phones");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(categoryRepository.findByParentCategory_IdOrderBySortOrder(1L)).thenReturn(List.of(child));
    when(categoryRepository.findByParentCategory_IdOrderBySortOrder(2L)).thenReturn(List.of());

    CategoryResponse response = catalogService.getCategoryWithChildren(1L);

    assertThat(response).isNotNull();
    assertThat(response.children()).hasSize(1);
  }

  @Test
  void testGetCategoryWithChildrenNotFound() {
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> catalogService.getCategoryWithChildren(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testGetCategoryChildren() {
    Category child = new Category();
    child.setId(2L);

    when(categoryRepository.findByParentCategory_IdOrderBySortOrder(1L)).thenReturn(List.of(child));

    List<CategoryResponse> children = catalogService.getCategoryChildren(1L);

    assertThat(children).hasSize(1);
  }

  @Test
  void testDeleteCategoryLogical() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

    catalogService.deleteCategoryLogical(1L);

    assertThat(testCategory.getActive()).isFalse();
  }
}
