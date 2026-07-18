package org.sirantar.recadero.catalog.api;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.catalog.service.ProductService;
import org.sirantar.recadero.catalog.service.ProductVariantService;
import org.sirantar.recadero.catalog.service.dto.ProductAvailabilityResponse;
import org.sirantar.recadero.catalog.service.dto.ProductCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductPriceUpdateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductResponse;
import org.sirantar.recadero.catalog.service.dto.ProductStatusUpdateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductUpdateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductVariantCreateRequest;
import org.sirantar.recadero.catalog.service.dto.ProductVariantResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product REST API.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ProductVariantService productVariantService;
  private final InventoryService inventoryService;

  @GetMapping
  public Page<ProductResponse> searchProducts(
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "category_id", required = false) Long categoryId,
      @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
      @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
      Pageable pageable) {
    return productService.searchProducts(query, categoryId, minPrice, maxPrice, pageable);
  }

  @GetMapping("/{id}")
  public ProductResponse getProduct(@PathVariable Long id) {
    return productService.getProductWithVariants(id);
  }

  @GetMapping("/{id}/availability")
  public ProductAvailabilityResponse getAvailability(@PathVariable Long id) {
    return inventoryService.getProductAvailability(id);
  }

  @PostMapping
  @AdminOnly
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse createProduct(@RequestBody ProductCreateRequest request) {
    return productService.createProduct(request);
  }

  @PutMapping("/{id}")
  @AdminOnly
  public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
    return productService.updateProduct(id, request);
  }

  @PatchMapping("/{id}/status")
  @AdminOnly
  public ProductResponse updateStatus(@PathVariable Long id, @RequestBody ProductStatusUpdateRequest request) {
    return productService.changeProductStatus(id, request);
  }

  @DeleteMapping("/{id}")
  @AdminOnly
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
  }

  @PostMapping("/{id}/variants")
  @AdminOnly
  @ResponseStatus(HttpStatus.CREATED)
  public ProductVariantResponse createVariant(
      @PathVariable Long id, @RequestBody ProductVariantCreateRequest request) {
    return productVariantService.createVariant(id, request);
  }

  @GetMapping("/{id}/variants")
  public java.util.List<ProductVariantResponse> getVariants(@PathVariable Long id) {
    return productVariantService.getVariantsForProduct(id);
  }

  @PatchMapping("/{id}/price")
  @AdminOnly
  public ProductResponse updatePrice(@PathVariable Long id, @RequestBody ProductPriceUpdateRequest request) {
    return productService.updateProductPrice(id, request.price());
  }
}
