package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for POST /api/v1/cart/clear.
 */
public record ClearCartResponse(String message, @JsonProperty("items_removed") int itemsRemoved) {}
