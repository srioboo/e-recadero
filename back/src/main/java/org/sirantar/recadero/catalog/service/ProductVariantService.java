package org.sirantar.recadero.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.dto.ProductVariantCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductVariantResponse;
import org.sirantar.recadero.catalog.service.dto.ProductVariantUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product variant management and SKU generation.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantService {

  private static final Pattern NON_ALNUM = Pattern.compile("[^A-Z0-9]+");

  private final ProductVariantRepository productVariantRepository;
  private final ProductRepository productRepository;

  public ProductVariantResponse createVariant(Long productId, ProductVariantCreateRequest request) {
    Product product = getProduct(productId);
    ProductVariant variant = new ProductVariant();
    variant.setProduct(product);
    variant.setSku(resolveSku(product, request.sku(), request.variantAttributes()));
    variant.setVariantAttributes(request.variantAttributes());
    variant.setPrice(request.price());
    variant.setWeight(request.weight());
    return toResponse(productVariantRepository.save(variant));
  }

  public ProductVariantResponse updateVariant(Long variantId, ProductVariantUpdateRequest request) {
    ProductVariant variant = getVariant(variantId);
    Product product = variant.getProduct();
    if (request.sku() != null) {
      variant.setSku(resolveSku(product, request.sku(), request.variantAttributes()));
    } else if (request.variantAttributes() != null) {
      variant.setSku(resolveSku(product, variant.getSku(), request.variantAttributes()));
    }
    if (request.variantAttributes() != null) {
      variant.setVariantAttributes(request.variantAttributes());
    }
    if (request.price() != null) {
      variant.setPrice(request.price());
    }
    if (request.weight() != null) {
      variant.setWeight(request.weight());
    }
    return toResponse(productVariantRepository.save(variant));
  }

  public void deleteVariant(Long variantId) {
    ProductVariant variant = getVariant(variantId);
    productVariantRepository.delete(variant);
  }

  @Transactional(readOnly = true)
  public List<ProductVariantResponse> getVariantsForProduct(Long productId) {
    return productVariantRepository.findByProductId(productId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public String generateSku(Long productId, String variantAttributes) {
    return resolveSku(getProduct(productId), null, variantAttributes);
  }

  private Product getProduct(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
  }

  private ProductVariant getVariant(Long variantId) {
    return productVariantRepository.findById(variantId)
        .orElseThrow(() -> new EntityNotFoundException("Variant not found: " + variantId));
  }

  private String resolveSku(Product product, String suppliedSku, String variantAttributes) {
    if (suppliedSku != null && !suppliedSku.isBlank()) {
      return suppliedSku;
    }
    String base = product.getSku() == null ? "SKU" : product.getSku();
    String suffix = variantAttributes == null || variantAttributes.isBlank()
        ? "VARIANT"
        : NON_ALNUM.matcher(variantAttributes.toUpperCase(Locale.ROOT)).replaceAll("-").replaceAll("^-|-$", "");
    return base + "-" + suffix;
  }

  private ProductVariantResponse toResponse(ProductVariant variant) {
    return new ProductVariantResponse(
        variant.getId(),
        variant.getSku(),
        variant.getVariantAttributes(),
        variant.getPrice(),
        variant.getWeight());
  }
}
