package org.sirantar.recadero.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.Category;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductStatus;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.events.ProductArchivedEvent;
import org.sirantar.recadero.catalog.events.ProductPriceChangedEvent;
import org.sirantar.recadero.catalog.events.ProductPublishedEvent;
import org.sirantar.recadero.catalog.repository.CategoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.dto.ProductCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductResponse;
import org.sirantar.recadero.catalog.service.dto.ProductVariantResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catalog product management.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CategoryRepository categoryRepository;
  private final ApplicationEventPublisher eventPublisher;

  public ProductResponse createProduct(ProductCreateRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.categoryId()));

    Product product = new Product();
    product.setCategory(category);
    product.setSku(request.sku());
    product.setName(request.name());
    product.setDescription(request.description());
    product.setShortDescription(request.shortDescription());
    product.setPrice(request.basePrice());
    product.setCostPrice(request.costPrice());
    product.setFeatured(Boolean.TRUE.equals(request.featured()));
    if (request.status() != null) {
      product.setStatus(ProductStatus.valueOf(request.status()));
    }

    return toResponse(productRepository.save(product));
  }

  public ProductResponse publishProduct(Long productId) {
    Product product = getProduct(productId);
    requirePublishable(product);
    product.setStatus(ProductStatus.PUBLISHED);
    Product saved = productRepository.save(product);
    eventPublisher.publishEvent(new ProductPublishedEvent(saved.getId()));
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Page<ProductResponse> searchProducts(
      String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
    return productRepository.search(query, categoryId, minPrice, maxPrice, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ProductResponse getProductWithVariants(Long productId) {
    Product product = getProduct(productId);
    List<ProductVariantResponse> variants = productVariantRepository.findByProductId(productId).stream()
        .map(this::toResponse)
        .toList();
    return toResponse(product, variants, List.of());
  }

  public ProductResponse updateProductPrice(Long productId, BigDecimal newPrice) {
    Product product = getProduct(productId);
    BigDecimal oldPrice = product.getPrice();
    product.setPrice(newPrice);
    Product saved = productRepository.save(product);
    eventPublisher.publishEvent(new ProductPriceChangedEvent(productId, oldPrice, newPrice));
    return toResponse(saved);
  }

  public void archiveProduct(Long productId) {
    Product product = getProduct(productId);
    product.setStatus(ProductStatus.ARCHIVED);
    productRepository.save(product);
    eventPublisher.publishEvent(new ProductArchivedEvent(productId));
  }

  private Product getProduct(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
  }

  private void requirePublishable(Product product) {
    if (product.getCategory() == null
        || product.getSku() == null
        || product.getName() == null
        || product.getPrice() == null
        || product.getDescription() == null) {
      throw new IllegalStateException("Product is missing required publish fields");
    }
  }

  private ProductResponse toResponse(Product product) {
    return toResponse(product, List.of(), List.of());
  }

  private ProductResponse toResponse(Product product, List<ProductVariantResponse> variants, List<String> images) {
    return new ProductResponse(
        product.getId(),
        product.getCategory() != null ? product.getCategory().getId() : null,
        product.getSku(),
        product.getName(),
        product.getDescription(),
        product.getShortDescription(),
        product.getPrice(),
        product.getCostPrice(),
        product.getStatus() != null ? product.getStatus().name() : null,
        product.getFeatured(),
        variants,
        images);
  }

  private ProductVariantResponse toResponse(ProductVariant productVariant) {
    return new ProductVariantResponse(
        productVariant.getId(),
        productVariant.getSku(),
        productVariant.getVariantAttributes(),
        productVariant.getPrice(),
        productVariant.getWeight());
  }
}
