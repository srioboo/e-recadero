package org.sirantar.recadero.catalog.service.dto;

/**
 * Request payload for updating a category.
 */
public record CategoryUpdateRequest(
    String name,
    String slug,
    String description,
    Long parentCategoryId,
    String imageUrl,
    Integer sortOrder,
    Boolean active) {}
