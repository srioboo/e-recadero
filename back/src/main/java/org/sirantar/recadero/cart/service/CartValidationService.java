package org.sirantar.recadero.cart.service;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Validation rules for cart item operations.
 */
@Service
@RequiredArgsConstructor
public class CartValidationService {

  private static final int MIN_QUANTITY = 1;
  private static final int MAX_QUANTITY = 1000;

  private final ProductVariantRepository productVariantRepository;

  public void validateQuantity(int quantity) {
    if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
      throw new BusinessLogicException(
          "INVALID_QUANTITY",
          "Quantity must be between " + MIN_QUANTITY + " and " + MAX_QUANTITY,
          java.util.Map.of("field", "quantity", "provided_value", quantity));
    }
  }

  public ProductVariant requireVariant(Long productVariantId) {
    return productVariantRepository.findById(productVariantId)
        .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + productVariantId));
  }
}
