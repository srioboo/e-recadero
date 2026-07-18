package org.sirantar.recadero.catalog.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.service.CatalogService;
import org.sirantar.recadero.catalog.service.dto.CategoryCreateRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryMoveRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryResponse;
import org.sirantar.recadero.catalog.service.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Category REST API.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CatalogService catalogService;

  @GetMapping
  public Page<CategoryResponse> listCategories(Pageable pageable) {
    return catalogService.listCategories(pageable);
  }

  @GetMapping("/{id}")
  public CategoryResponse getCategory(@PathVariable Long id) {
    return catalogService.getCategoryWithChildren(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse createCategory(@RequestBody CategoryCreateRequest request) {
    return catalogService.createCategory(request);
  }

  @PutMapping("/{id}")
  public CategoryResponse updateCategory(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
    return catalogService.updateCategory(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCategory(@PathVariable Long id) {
    catalogService.deleteCategoryLogical(id);
  }

  @GetMapping("/{id}/children")
  public List<CategoryResponse> getChildren(@PathVariable Long id) {
    return catalogService.getCategoryChildren(id);
  }

  @PostMapping("/{id}/move")
  public CategoryResponse moveCategory(@PathVariable Long id, @RequestBody CategoryMoveRequest request) {
    return catalogService.moveCategory(id, request);
  }
}
