package org.sirantar.recadero.catalog.service.dto;

import java.util.List;

/**
 * Category view model with recursive children.
 */
public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Long parentCategoryId,
    String imageUrl,
    Boolean active,
    Integer sortOrder,
    List<CategoryResponse> children) {}
