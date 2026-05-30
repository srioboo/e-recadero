package org.sirantar.recadero.catalog.service.dto;

/**
 * Request payload for moving a category under a new parent.
 */
public record CategoryMoveRequest(Long parentCategoryId) {}
