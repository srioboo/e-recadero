package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/cart/items.
 */
public record AddItemRequest(@JsonProperty("product_variant_id") Long productVariantId, int quantity) {}
