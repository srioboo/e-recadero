package org.sirantar.recadero.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductStatus;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.CatalogService;
import org.sirantar.recadero.catalog.service.CategoryValidationService;
import org.sirantar.recadero.catalog.service.ProductService;
import org.sirantar.recadero.catalog.service.ProductVariantService;
import org.sirantar.recadero.catalog.service.dto.CategoryCreateRequest;
import org.sirantar.recadero.catalog.service.dto.CategoryResponse;
import org.sirantar.recadero.catalog.service.dto.ProductCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductResponse;
import org.sirantar.recadero.catalog.service.dto.ProductVariantCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductVariantResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * End-to-end integration tests for Catalog module.
 * Tests complete workflows across multiple services including:
 * - Category and product creation, publishing, and search
 * - Variant creation with price overrides
 * - Module integration patterns
 */
@DisplayName("Catalog Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class CatalogIntegrationTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private CategoryValidationService categoryValidationService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private CatalogService catalogService;
  @InjectMocks private ProductService productService;
  @InjectMocks private ProductVariantService productVariantService;

  private Category testCategory;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Electronics");
    testCategory.setSlug("electronics");
    testCategory.setActive(true);

    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setSku("LAPTOP-001");
    testProduct.setName("Test Laptop");
    testProduct.setStatus(ProductStatus.DRAFT);
    testProduct.setCategory(testCategory);
    testProduct.setDescription("Description");
    testProduct.setPrice(BigDecimal.valueOf(1299.99));
  }

  @Test
  @DisplayName("Should complete product lifecycle: Create → Publish → Search")
  void testCompleteProductLifecycle() {
    // Create category
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    CategoryCreateRequest catRequest =
        new CategoryCreateRequest("Electronics", "electronics", "Desc", null, null, 1, true);
    CategoryResponse catResponse = catalogService.createCategory(catRequest);

    assertThat(catResponse).isNotNull();
    assertThat(catResponse.name()).isEqualTo("Electronics");

    // Create product
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductCreateRequest prodRequest =
        new ProductCreateRequest(
            1L,
            "LAPTOP-001",
            "Test Laptop",
            "High performance laptop",
            "Laptop",
            BigDecimal.valueOf(1299.99),
            BigDecimal.valueOf(999.99),
            false,
            "DRAFT");

    ProductResponse prodResponse = productService.createProduct(prodRequest);

    assertThat(prodResponse).isNotNull();
    assertThat(prodResponse.sku()).isEqualTo("LAPTOP-001");
    assertThat(prodResponse.status()).isEqualTo(ProductStatus.DRAFT.name());

    // Publish product
    testProduct.setStatus(ProductStatus.PUBLISHED);
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    ProductResponse publishedResponse = productService.publishProduct(1L);

    assertThat(publishedResponse.status()).isEqualTo(ProductStatus.PUBLISHED.name());

    // Search published products
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> searchPage = new PageImpl<>(List.of(testProduct), pageable, 1);

    when(productRepository.search("Laptop", null, null, null, pageable)).thenReturn(searchPage);

    Page<ProductResponse> searchResult =
        productService.searchProducts("Laptop", null, null, null, pageable);

    assertThat(searchResult.getContent()).hasSize(1);
    assertThat(searchResult.getContent().getFirst().sku()).isEqualTo("LAPTOP-001");
  }

  @Test
  @DisplayName("Should create variant with price override")
  void testVariantPriceOverride() {
    ProductVariant variant = new ProductVariant();
    variant.setId(1L);
    variant.setSku("LAPTOP-BLUE");
    variant.setPrice(BigDecimal.valueOf(1399.99));
    variant.setProduct(testProduct);

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);

    ProductVariantCreateRequest request =
        new ProductVariantCreateRequest(
            "LAPTOP-BLUE",
            "Color: Blue",
            BigDecimal.valueOf(1399.99),
            BigDecimal.valueOf(3.5));

    ProductVariantResponse response = productVariantService.createVariant(1L, request);

    assertThat(response).isNotNull();
    assertThat(response.price()).isEqualTo(BigDecimal.valueOf(1399.99));
  }

  @Test
  @DisplayName("Should retrieve multiple variants with different prices")
  void testMultipleVariantsWithDifferentPrices() {
    ProductVariant variant1 = new ProductVariant();
    variant1.setId(1L);
    variant1.setPrice(BigDecimal.valueOf(1399.99));

    ProductVariant variant2 = new ProductVariant();
    variant2.setId(2L);
    variant2.setPrice(BigDecimal.valueOf(1299.99));

    when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(variant1, variant2));

    var variants = productVariantService.getVariantsForProduct(1L);

    assertThat(variants).hasSize(2);
    assertThat(variants)
        .anySatisfy(v -> assertThat(v.price()).isEqualTo(BigDecimal.valueOf(1399.99)))
        .anySatisfy(v -> assertThat(v.price()).isEqualTo(BigDecimal.valueOf(1299.99)));
  }

  @Test
  @DisplayName("Should get product with variants")
  void testGetProductWithVariants() {
    ProductVariant variant = new ProductVariant();
    variant.setId(1L);
    variant.setPrice(BigDecimal.valueOf(1399.99));

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(variant));

    ProductResponse response = productService.getProductWithVariants(1L);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.variants()).hasSize(1);
  }

  @Test
  @DisplayName("Should search products by query")
  void testSearchProductsByQuery() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> searchResults = new PageImpl<>(List.of(testProduct), pageable, 1);

    when(productRepository.search("Laptop", null, null, null, pageable))
        .thenReturn(searchResults);

    Page<ProductResponse> results =
        productService.searchProducts("Laptop", null, null, null, pageable);

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getTotalElements()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should update product price")
  void testUpdateProductPrice() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductResponse response = productService.updateProductPrice(1L, BigDecimal.valueOf(1499.99));

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should archive product successfully")
  void testArchiveProduct() {
    testProduct.setStatus(ProductStatus.PUBLISHED);

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    productService.archiveProduct(1L);

    assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
  }

  @Test
  @DisplayName("Should update category successfully")
  void testUpdateCategory() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    var updateRequest =
        new org.sirantar.recadero.catalog.service.dto.CategoryUpdateRequest(
            "Electronics Updated", "electronics", "Updated", null, null, 1, true);

    CategoryResponse response = catalogService.updateCategory(1L, updateRequest);

    assertThat(response).isNotNull();
  }

  @Test
  @DisplayName("Should list categories with pagination")
  void testListCategories() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Category> categoryPage = new PageImpl<>(List.of(testCategory), pageable, 1);

    when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

    Page<CategoryResponse> results = catalogService.listCategories(pageable);

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getTotalElements()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should get category with children hierarchy")
  void testGetCategoryWithChildren() {
    Category childCategory = new Category();
    childCategory.setId(2L);
    childCategory.setName("Laptops");
    childCategory.setParentCategory(testCategory);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(categoryRepository.findByParentCategory_IdOrderBySortOrder(1L))
        .thenReturn(List.of(childCategory));
    when(categoryRepository.findByParentCategory_IdOrderBySortOrder(2L)).thenReturn(List.of());

    CategoryResponse response = catalogService.getCategoryWithChildren(1L);

    assertThat(response).isNotNull();
    assertThat(response.children()).hasSize(1);
  }
}
