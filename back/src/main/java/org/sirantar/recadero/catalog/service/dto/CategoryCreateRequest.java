package org.sirantar.recadero.catalog.service.dto;

/**
 * Request payload for creating a category.
 */
public record CategoryCreateRequest(
    String name,
    String slug,
    String description,
    Long parentCategoryId,
    String imageUrl,
    Integer sortOrder,
    Boolean active) {}
