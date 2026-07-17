package org.sirantar.recadero.cart.service.dto;

/**
 * Request payload for PUT /api/v1/cart/items/{cart_item_id}.
 */
public record UpdateItemRequest(int quantity) {}
