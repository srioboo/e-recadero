package org.sirantar.recadero.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductStatus;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.dto.ProductCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductResponse;
import org.sirantar.recadero.catalog.service.dto.ProductStatusUpdateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductUpdateRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProductService productService;

  private Product testProduct;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Electronics");

    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setSku("PROD-001");
    testProduct.setName("Test Product");
    testProduct.setPrice(BigDecimal.valueOf(99.99));
    testProduct.setStatus(ProductStatus.DRAFT);
    testProduct.setCategory(testCategory);
    testProduct.setDescription("Valid description");
  }

  @Test
  void testCreateProductSuccess() {
    ProductCreateRequest request = new ProductCreateRequest(
        1L, "PROD-001", "Test Product", "Description", "Short desc",
        BigDecimal.valueOf(99.99), BigDecimal.valueOf(50.00), false, "DRAFT");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductResponse response = productService.createProduct(request);

    assertThat(response).isNotNull();
    assertThat(response.sku()).isEqualTo("PROD-001");
  }

  @Test
  void testCreateProductCategoryNotFound() {
    ProductCreateRequest request = new ProductCreateRequest(
        999L, "PROD-001", "Test Product", "Desc", "Short",
        BigDecimal.valueOf(99.99), BigDecimal.valueOf(50.00), false, "DRAFT");

    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.createProduct(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testPublishProductSuccess() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductResponse response = productService.publishProduct(1L);

    assertThat(response).isNotNull();
  }

  @Test
  void testPublishProductNotFound() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.publishProduct(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testSearchProducts() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);

    when(productRepository.search("test", 1L, null, null, pageable)).thenReturn(productPage);

    Page<ProductResponse> result = productService.searchProducts("test", 1L, null, null, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void testGetProductWithVariants() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productVariantRepository.findByProductId(1L)).thenReturn(List.of());

    ProductResponse response = productService.getProductWithVariants(1L);

    assertThat(response).isNotNull();
    assertThat(response.variants()).isEmpty();
  }

  @Test
  void testGetProductWithVariantsNotFound() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductWithVariants(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testUpdateProductPriceSuccess() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductResponse response = productService.updateProductPrice(1L, BigDecimal.valueOf(149.99));

    assertThat(response).isNotNull();
  }

  @Test
  void testUpdateProductPriceNotFound() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateProductPrice(999L, BigDecimal.valueOf(149.99)))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testChangeProductStatusSuccess() {
    testProduct.setStatus(ProductStatus.PUBLISHED);
    ProductStatusUpdateRequest request = new ProductStatusUpdateRequest("ARCHIVED");

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    ProductResponse response = productService.changeProductStatus(1L, request);

    assertThat(response).isNotNull();
  }

  @Test
  void testArchiveProductSuccess() {
    testProduct.setStatus(ProductStatus.PUBLISHED);

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    productService.archiveProduct(1L);

    assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
  }

  @Test
  void testArchiveProductNotFound() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.archiveProduct(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
